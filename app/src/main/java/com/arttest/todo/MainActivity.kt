package com.arttest.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arttest.todo.ui.screens.HomeScreen
import com.arttest.todo.ui.screens.EditTodoScreen
import com.arttest.todo.ui.theme.ToDoAppTheme
import com.arttest.todo.viewmodel.TodoViewModel
import com.arttest.todo.data.TodoItem
import com.arttest.todo.data.Category
import com.arttest.todo.viewmodel.TodoFilterType

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[TodoViewModel::class.java]

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

    @Composable
    private fun TodoAppContent() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val filterState by viewModel.filterState.collectAsState()

        // 导航状态
        var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
        var showAddDialog by remember { mutableStateOf(false) }

        if (editingTodo != null) {
            // 编辑屏幕
            EditTodoScreen(
                todo = editingTodo!!,
                onBack = { editingTodo = null },
                onSave = { todo ->
                    viewModel.updateTodo(todo)
                    editingTodo = null
                },
                onDelete = { todo ->
                    viewModel.deleteTodo(todo)
                    editingTodo = null
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
                onDeleteCompleted = { viewModel.deleteCompleted() }
            )
        }

        // 添加 Todo 对话框
        if (showAddDialog) {
            com.arttest.todo.ui.components.AddEditTodoDialog(
                todo = null,
                onDismiss = { showAddDialog = false },
                onSave = { todo ->
                    viewModel.insertTodo(todo)
                    showAddDialog = false
                }
            )
        }
    }
}
