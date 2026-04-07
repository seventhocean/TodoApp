package com.arttest.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arttest.todo.data.*
import com.arttest.todo.notification.NotificationHelper
import com.arttest.todo.notification.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
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
 * UI 状态
 */
data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val filteredTodos: List<TodoItem> = emptyList(),
    val filterState: FilterState = FilterState(),
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val isLoading: Boolean = true
)

/**
 * Todo ViewModel
 */
class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TodoDatabase.getDatabase(application).todoDao()
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
                _uiState.value = state
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
}
