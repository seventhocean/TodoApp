package com.arttest.todo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arttest.todo.ui.screens.HomeScreen
import com.arttest.todo.ui.screens.EditTodoScreen
import com.arttest.todo.ui.theme.ToDoAppTheme
import com.arttest.todo.viewmodel.TodoViewModel
import com.arttest.todo.data.TodoItem
import com.arttest.todo.data.Category
import com.arttest.todo.viewmodel.TodoFilterType
import com.arttest.todo.notification.ReminderWorker

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TodoViewModel
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予
        } else {
            // 权限被拒绝
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[TodoViewModel::class.java]

        // 请求通知权限（Android 13+）
        requestNotificationPermission()

        enableEdgeToEdge()

        setContent {
            ToDoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    TodoAppContent()
                }
            }
        }
    }

    /**
     * 请求通知权限（Android 13+）
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    @Composable
    private fun TodoAppContent() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val filterState by viewModel.filterState.collectAsState()
        val subTasksMap by viewModel.subTasksMap.collectAsStateWithLifecycle()

        // 导航状态
        var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }

        if (editingTodo != null) {
            // 编辑屏幕
            EditTodoScreen(
                todo = editingTodo!!,
                subTasks = subTasksMap[editingTodo!!.id] ?: emptyList(),
                onBack = { editingTodo = null },
                onSave = { todo ->
                    viewModel.updateTodo(todo)
                    editingTodo = null
                },
                onDelete = { todo ->
                    viewModel.deleteTodo(todo)
                    editingTodo = null
                },
                onAddSubTask = { title, description, priority ->
                    viewModel.addSubTask(editingTodo!!.id, title, description, priority)
                },
                onToggleSubTask = { subTask ->
                    viewModel.toggleSubTaskComplete(subTask)
                },
                onDeleteSubTask = { subTask ->
                    viewModel.deleteSubTask(subTask)
                }
            )
        } else {
            // 主屏幕
            HomeScreen(
                uiState = uiState,
                filterState = filterState,
                onAddTodo = { showAddDialog = true },
                onEditTodo = { todo -> editingTodo = todo },
                onToggleComplete = { todo -> viewModel.toggleComplete(todo) },
                onDeleteTodo = { todo -> viewModel.deleteTodo(todo) },
                onFilterTypeSelected = { type -> viewModel.setFilterType(type) },
                onCategorySelected = { category -> viewModel.setCategoryFilter(category) },
                onSearchQueryChanged = { query -> viewModel.setSearchQuery(query) },
                onDeleteCompleted = { viewModel.deleteCompleted() },
                onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                onToggleItemSelection = { todoId -> viewModel.toggleSelection(todoId) },
                onBatchComplete = { viewModel.batchMarkCompleted() },
                onBatchDelete = { viewModel.batchDelete() },
                onBatchActive = { viewModel.batchMarkActive() },
                onSelectAll = { viewModel.selectAll() }
            )
        }

        // 添加 Todo 对话框
        if (showAddDialog) {
            com.arttest.todo.ui.components.AddEditTodoDialog(
                todo = null,
                onDismiss = { showAddDialog = false },
                onSave = { todo ->
                    viewModel.insertTodo(todo)
                    // 设置提醒
                    if (todo.hasReminder && todo.reminderTime != null) {
                        viewModel.scheduleReminder(todo)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}
