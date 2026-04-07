package com.arttest.todo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arttest.todo.data.Category
import com.arttest.todo.data.TodoItem
import com.arttest.todo.ui.components.*
import com.arttest.todo.viewmodel.FilterState
import com.arttest.todo.viewmodel.TodoFilterType
import com.arttest.todo.viewmodel.TodoUiState

/**
 * 主屏幕 - 包含顶部栏、筛选器和 Todo 列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: TodoUiState,
    filterState: FilterState,
    onAddTodo: () -> Unit,
    onEditTodo: (TodoItem) -> Unit,
    onToggleComplete: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    onFilterTypeSelected: (TodoFilterType) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDeleteCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部栏
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "待办事项",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.activeCount} 未完成 / ${uiState.totalCount} 总计",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // 删除已完成按钮
                    if (uiState.completedCount > 0) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "删除已完成",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            // 搜索栏
            TodoSearchBar(
                query = filterState.searchQuery,
                onQueryChange = onSearchQueryChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 筛选 Chip 组
            FilterChipGroup(
                selectedFilter = when (filterState.filterType) {
                    TodoFilterType.ALL -> TodoFilter.ALL
                    TodoFilterType.ACTIVE -> TodoFilter.ACTIVE
                    TodoFilterType.COMPLETED -> TodoFilter.COMPLETED
                    TodoFilterType.HIGH_PRIORITY -> TodoFilter.HIGH_PRIORITY
                    TodoFilterType.DUE_SOON -> TodoFilter.DUE_SOON
                },
                onFilterSelected = { filter ->
                    val filterType = when (filter) {
                        TodoFilter.ALL -> TodoFilterType.ALL
                        TodoFilter.ACTIVE -> TodoFilterType.ACTIVE
                        TodoFilter.COMPLETED -> TodoFilterType.COMPLETED
                        TodoFilter.HIGH_PRIORITY -> TodoFilterType.HIGH_PRIORITY
                        TodoFilter.DUE_SOON -> TodoFilterType.DUE_SOON
                        TodoFilter.BY_CATEGORY -> TodoFilterType.ALL
                    }
                    onFilterTypeSelected(filterType)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.CenterEnd)
            )

            // 分类筛选（当选择按分类筛选时显示）
            if (filterState.filterType == TodoFilterType.ALL) {
                CategorySelector(
                    selectedCategory = filterState.category ?: Category.OTHER,
                    onCategorySelected = { cat ->
                        onCategorySelected(if (cat == Category.OTHER) null else cat)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Todo 列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTodos.isEmpty()) {
                EmptyState(
                    message = if (filterState.searchQuery.isNotBlank()) {
                        "没有找到匹配的待办事项"
                    } else {
                        "暂无待办事项，点击 + 添加第一个吧！"
                    },
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // 为 FAB 留空间
                ) {
                    items(
                        items = uiState.filteredTodos,
                        key = { it.id }
                    ) { todo ->
                        TodoItemCard(
                            todo = todo,
                            onToggleComplete = { onToggleComplete(todo) },
                            onEdit = { onEditTodo(todo) },
                            onDelete = { onDeleteTodo(todo) }
                        )
                    }
                }
            }
        }

        // 悬浮操作按钮 (FAB)
        FloatingActionButton(
            onClick = onAddTodo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加待办",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("删除已完成") },
            text = { Text("确定要删除所有已完成的待办事项吗？此操作不可恢复。") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCompleted()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}
