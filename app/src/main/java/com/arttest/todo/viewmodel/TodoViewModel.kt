package com.arttest.todo.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arttest.todo.data.*
import com.arttest.todo.notification.NotificationHelper
import com.arttest.todo.notification.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 筛选条件状态
 */
data class FilterState(
    val filterType: TodoFilterType = TodoFilterType.ALL,
    val category: Category? = null,
    val searchQuery: String = ""
)

enum class TodoFilterType {
    ALL,
    ACTIVE,
    COMPLETED,
    HIGH_PRIORITY,
    DUE_SOON
}

/**
 * 统计数据
 */
data class TodoStatistics(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val completionRate: Float = 0f,
    val categoryDistribution: Map<String, Int> = emptyMap(),
    val priorityDistribution: Map<String, Int> = emptyMap(),
    val weeklyCompletionData: List<Pair<String, Int>> = emptyList()
)

/**
 * UI 状态
 */
data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val filteredTodos: List<TodoItem> = emptyList(),
    val filterState: FilterState = FilterState(),
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val isLoading: Boolean = true,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
)

/**
 * Todo ViewModel
 */
class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TodoDatabase.getDatabase(application).todoDao()
    private val subTaskDao = TodoDatabase.getDatabase(application).subTaskDao()
    private val notificationHelper = NotificationHelper(application)

    // 所有 Todo 流
    private val allTodosFlow: Flow<List<TodoItem>> = dao.getAllTodos()

    // 统计流
    private val totalCountFlow: Flow<Int> = dao.getTotalCount()
    private val activeCountFlow: Flow<Int> = dao.getActiveCount()
    private val completedCountFlow: Flow<Int> = dao.getCompletedCount()

    // 筛选状态
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // UI 状态
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    // 子任务流
    private val _subTasksMap = MutableStateFlow<Map<Long, List<SubTask>>>(emptyMap())
    val subTasksMap: StateFlow<Map<Long, List<SubTask>>> = _subTasksMap.asStateFlow()

    // 批量选择模式
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    // 统计数据
    private val _statistics = MutableStateFlow(TodoStatistics())
    val statistics: StateFlow<TodoStatistics> = _statistics.asStateFlow()

    init {
        // 收集所有数据并组合
        viewModelScope.launch {
            combine(
                allTodosFlow,
                totalCountFlow,
                activeCountFlow,
                completedCountFlow,
                _filterState
            ) { todos, total, active, completed, filter ->
                TodoUiState(
                    todos = todos,
                    filteredTodos = applyFilter(todos, filter),
                    filterState = filter,
                    totalCount = total,
                    activeCount = active,
                    completedCount = completed,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    isSelectionMode = _selectionMode.value,
                    selectedIds = _selectedIds.value
                )
            }
        }

        // 收集统计数据
        viewModelScope.launch {
            allTodosFlow.collect { todos ->
                val completed = todos.count { it.isCompleted }
                val active = todos.count { !it.isCompleted }
                val completionRate = if (todos.isNotEmpty()) completed.toFloat() / todos.size else 0f

                // 分类分布
                val categoryDist = todos.groupingBy { it.category.name }.eachCount()

                // 优先级分布
                val priorityDist = todos.groupingBy { it.priority.name }.eachCount()

                // 每周完成数据（过去 7 天）
                val weeklyData = (0..6).map { daysAgo ->
                    val date = LocalDate.now().minusDays(daysAgo.toLong())
                    val count = todos.count { todo ->
                        todo.isCompleted && todo.updatedAt.toLocalDate() == date
                    }
                    val dayName = when (daysAgo) {
                        0 -> "今天"
                        1 -> "昨天"
                        else -> "${daysAgo}天前"
                    }
                    dayName to count
                }.reversed()

                _statistics.value = TodoStatistics(
                    totalCount = todos.size,
                    completedCount = completed,
                    activeCount = active,
                    completionRate = completionRate,
                    categoryDistribution = categoryDist,
                    priorityDistribution = priorityDist,
                    weeklyCompletionData = weeklyData
                )
            }
        }

        // 收集所有子任务数据
        viewModelScope.launch {
            allTodosFlow.collect { todos ->
                val subTasksMap = mutableMapOf<Long, List<SubTask>>()
                todos.forEach { todo ->
                    subTaskDao.getSubTasksByParentIdSync(todo.id).let { subTasks ->
                        subTasksMap[todo.id] = subTasks
                    }
                }
                _subTasksMap.value = subTasksMap
            }
        }
    }

    /**
     * 应用筛选逻辑
     */
    private fun applyFilter(todos: List<TodoItem>, filter: FilterState): List<TodoItem> {
        return todos
            .asSequence()
            .filter { todo ->
                // 搜索过滤
                if (filter.searchQuery.isNotBlank()) {
                    val query = filter.searchQuery.lowercase()
                    todo.title.lowercase().contains(query) ||
                    todo.description.lowercase().contains(query)
                } else true
            }
            .filter { todo ->
                // 类型过滤
                when (filter.filterType) {
                    TodoFilterType.ALL -> true
                    TodoFilterType.ACTIVE -> !todo.isCompleted
                    TodoFilterType.COMPLETED -> todo.isCompleted
                    TodoFilterType.HIGH_PRIORITY -> todo.priority == Priority.HIGH
                    TodoFilterType.DUE_SOON -> {
                        todo.dueDate != null &&
                        !todo.isCompleted &&
                        todo.dueDate.isAfter(LocalDate.now().minusDays(1)) &&
                        todo.dueDate.isBefore(LocalDate.now().plusDays(4))
                    }
                }
            }
            .filter { todo ->
                // 分类过滤
                filter.category?.let { todo.category == it } ?: true
            }
            .sortedWith(
                compareByDescending<TodoItem> { it.isCompleted }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.dueDate ?: LocalDate.MAX }
            )
            .toList()
    }

    // ============ 操作_methods ============

    /**
     * 设置筛选类型
     */
    fun setFilterType(type: TodoFilterType) {
        _filterState.value = _filterState.value.copy(filterType = type)
    }

    /**
     * 设置分类筛选
     */
    fun setCategoryFilter(category: Category?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    /**
     * 设置搜索查询
     */
    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    /**
     * 重置筛选
     */
    fun resetFilters() {
        _filterState.value = FilterState()
    }

    /**
     * 插入 Todo
     */
    fun insertTodo(todo: TodoItem) {
        viewModelScope.launch {
            dao.insert(todo)
        }
    }

    /**
     * 更新 Todo
     */
    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            dao.update(todo)
        }
    }

    /**
     * 删除 Todo
     */
    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            dao.delete(todo)
        }
    }

    /**
     * 切换完成状态
     */
    fun toggleComplete(todo: TodoItem) {
        viewModelScope.launch {
            dao.toggleComplete(
                id = todo.id,
                isCompleted = !todo.isCompleted,
                updatedAt = LocalDateTime.now()
            )
        }
    }

    /**
     * 删除所有已完成
     */
    fun deleteCompleted() {
        viewModelScope.launch {
            dao.deleteCompleted()
        }
    }

    /**
     * 获取单个 Todo
     */
    suspend fun getTodoById(id: Long): TodoItem? {
        return dao.getTodoById(id)
    }

    // ============ 提醒管理 ============

    /**
     * 设置提醒
     */
    fun scheduleReminder(todo: TodoItem) {
        todo.reminderTime?.let { reminderTime ->
            val now = LocalDateTime.now()
            val delayMillis = ChronoUnit.MILLIS.between(now, reminderTime)

            if (delayMillis > 0) {
                ReminderWorker.scheduleOneTimeReminder(
                    context = getApplication(),
                    todoId = todo.id,
                    delayMillis = delayMillis
                )
            }
        }
    }

    /**
     * 取消提醒
     */
    fun cancelReminder(todo: TodoItem) {
        ReminderWorker.cancelReminder(getApplication(), todo.id)
        notificationHelper.cancelNotification(todo.id)
    }

    /**
     * 删除待办时取消提醒
     */
    fun deleteTodoWithReminder(todo: TodoItem) {
        viewModelScope.launch {
            cancelReminder(todo)
            dao.delete(todo)
        }
    }

    /**
     * 切换完成状态时管理提醒
     */
    fun toggleCompleteWithReminder(todo: TodoItem) {
        viewModelScope.launch {
            if (!todo.isCompleted) {
                // 标记为完成时取消提醒
                cancelReminder(todo)
            }
            dao.toggleComplete(
                id = todo.id,
                isCompleted = !todo.isCompleted,
                updatedAt = LocalDateTime.now()
            )
        }
    }

    // ============ 子任务管理 ============

    /**
     * 获取子任务列表
     */
    fun getSubTasks(todoId: Long): List<SubTask> {
        return _subTasksMap.value[todoId] ?: emptyList()
    }

    /**
     * 添加子任务
     */
    fun addSubTask(parentTodoId: Long, title: String, description: String = "", priority: Priority = Priority.MEDIUM) {
        viewModelScope.launch {
            val subTask = SubTask(
                parentTodoId = parentTodoId,
                title = title,
                description = description,
                priority = priority,
                sortOrder = _subTasksMap.value[parentTodoId]?.size ?: 0
            )
            subTaskDao.insert(subTask)
        }
    }

    /**
     * 切换子任务完成状态
     */
    fun toggleSubTaskComplete(subTask: SubTask) {
        viewModelScope.launch {
            subTaskDao.toggleComplete(subTask.id, !subTask.isCompleted)
        }
    }

    /**
     * 更新子任务
     */
    fun updateSubTask(subTask: SubTask) {
        viewModelScope.launch {
            subTaskDao.update(subTask)
        }
    }

    /**
     * 删除子任务
     */
    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            subTaskDao.delete(subTask)
        }
    }

    // ============ 批量操作 ============

    /**
     * 切换选择模式
     */
    fun toggleSelectionMode() {
        _selectionMode.value = !_selectionMode.value
        if (!_selectionMode.value) {
            _selectedIds.value = emptySet()
        }
    }

    /**
     * 切换单个待办选择状态
     */
    fun toggleSelection(todoId: Long) {
        _selectedIds.value = if (todoId in _selectedIds.value) {
            _selectedIds.value - todoId
        } else {
            _selectedIds.value + todoId
        }
    }

    /**
     * 全选
     */
    fun selectAll() {
        _selectedIds.value = _uiState.value.filteredTodos.map { it.id }.toSet()
    }

    /**
     * 取消所有选择
     */
    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    /**
     * 批量标记完成
     */
    fun batchMarkCompleted() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                val todo = dao.getTodoById(id)
                todo?.let {
                    dao.toggleComplete(id, true, LocalDateTime.now())
                    if (it.hasReminder && it.reminderTime != null) {
                        cancelReminder(it)
                    }
                }
            }
            _selectedIds.value = emptySet()
            _selectionMode.value = false
        }
    }

    /**
     * 批量删除
     */
    fun batchDelete() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                val todo = dao.getTodoById(id)
                todo?.let {
                    if (it.hasReminder) {
                        cancelReminder(it)
                    }
                    dao.delete(it)
                }
            }
            _selectedIds.value = emptySet()
            _selectionMode.value = false
        }
    }

    /**
     * 批量标记未完成
     */
    fun batchMarkActive() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                dao.toggleComplete(id, false, LocalDateTime.now())
            }
            _selectedIds.value = emptySet()
            _selectionMode.value = false
        }
    }

    // ============ 导出/导入 ============

    /**
     * 导出待办事项到 JSON 文件
     */
    fun exportToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val todos = dao.getAllTodos().first()
                val subTasks = TodoDatabase.getDatabase(context).subTaskDao()
                    .let { dao ->
                        val allSubTasks = mutableListOf<SubTask>()
                        todos.forEach { todo ->
                            allSubTasks.addAll(dao.getSubTasksByParentIdSync(todo.id))
                        }
                        allSubTasks
                    }

                val jsonArray = JSONArray()
                todos.forEach { todo ->
                    val todoJson = JSONObject().apply {
                        put("id", todo.id)
                        put("title", todo.title)
                        put("description", todo.description)
                        put("isCompleted", todo.isCompleted)
                        put("priority", todo.priority.name)
                        put("category", todo.category.name)
                        put("dueDate", todo.dueDate?.toString())
                        put("createdAt", todo.createdAt.toString())
                        put("updatedAt", todo.updatedAt.toString())
                        put("hasReminder", todo.hasReminder)
                        put("reminderTime", todo.reminderTime?.toString())
                        put("repeatType", todo.repeatType.name)
                        put("repeatEndDate", todo.repeatEndDate?.toString())
                    }

                    // 添加子任务
                    val subTasksJson = JSONArray()
                    subTasks.filter { it.parentTodoId == todo.id }.forEach { subTask ->
                        subTasksJson.put(JSONObject().apply {
                            put("id", subTask.id)
                            put("parentTodoId", subTask.parentTodoId)
                            put("title", subTask.title)
                            put("description", subTask.description)
                            put("isCompleted", subTask.isCompleted)
                            put("priority", subTask.priority.name)
                            put("dueDate", subTask.dueDate?.toString())
                            put("sortOrder", subTask.sortOrder)
                        })
                    }
                    todoJson.put("subTasks", subTasksJson)
                    jsonArray.put(todoJson)
                }

                // 写入文件
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonArray.toString(2).toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 从 JSON 文件导入待办事项
     */
    fun importFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonStr = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: return@launch

                val jsonArray = JSONArray(jsonStr)
                val subTaskDao = TodoDatabase.getDatabase(context).subTaskDao()

                for (i in 0 until jsonArray.length()) {
                    val todoJson = jsonArray.getJSONObject(i)

                    // 映射旧 ID 到新 ID
                    val oldId = todoJson.getLong("id")

                    val todoItem = TodoItem(
                        id = 0, // 新插入，使用自增 ID
                        title = todoJson.getString("title"),
                        description = todoJson.optString("description", ""),
                        isCompleted = todoJson.optBoolean("isCompleted", false),
                        priority = try {
                            Priority.valueOf(todoJson.getString("priority"))
                        } catch (e: Exception) {
                            Priority.MEDIUM
                        },
                        category = try {
                            Category.valueOf(todoJson.getString("category"))
                        } catch (e: Exception) {
                            Category.OTHER
                        },
                        dueDate = todoJson.optString("dueDate", null)?.let { LocalDate.parse(it) },
                        createdAt = todoJson.optString("createdAt", null)?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        hasReminder = todoJson.optBoolean("hasReminder", false),
                        reminderTime = todoJson.optString("reminderTime", null)?.let { LocalDateTime.parse(it) },
                        repeatType = try {
                            RepeatType.valueOf(todoJson.optString("repeatType", "NONE"))
                        } catch (e: Exception) {
                            RepeatType.NONE
                        },
                        repeatEndDate = todoJson.optString("repeatEndDate", null)?.let { LocalDate.parse(it) }
                    )

                    val newId = dao.insert(todoItem)

                    // 导入子任务
                    todoJson.optJSONArray("subTasks")?.let { subTasksJson ->
                        for (j in 0 until subTasksJson.length()) {
                            val subTaskJson = subTasksJson.getJSONObject(j)
                            val subTask = SubTask(
                                id = 0,
                                parentTodoId = newId,
                                title = subTaskJson.getString("title"),
                                description = subTaskJson.optString("description", ""),
                                isCompleted = subTaskJson.optBoolean("isCompleted", false),
                                priority = try {
                                    Priority.valueOf(subTaskJson.getString("priority"))
                                } catch (e: Exception) {
                                    Priority.MEDIUM
                                },
                                dueDate = subTaskJson.optString("dueDate", null)?.let { LocalDate.parse(it) },
                                sortOrder = subTaskJson.optInt("sortOrder", j)
                            )
                            subTaskDao.insert(subTask)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
