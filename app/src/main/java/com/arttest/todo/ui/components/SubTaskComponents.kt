package com.arttest.todo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.arttest.todo.data.Priority
import com.arttest.todo.data.SubTask

/**
 * 子任务列表组件
 */
@Composable
fun SubTaskList(
    subTasks: List<SubTask>,
    parentTodoId: Long,
    onToggleSubTask: (SubTask) -> Unit,
    onEditSubTask: (SubTask) -> Unit,
    onDeleteSubTask: (SubTask) -> Unit,
    onAddSubTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "子任务 (${subTasks.count { it.isCompleted }}/${subTasks.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = onAddSubTask,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加子任务",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // 子任务列表
        if (subTasks.isEmpty()) {
            Text(
                text = "暂无子任务，点击右上角添加",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            subTasks.forEach { subTask ->
                SubTaskItem(
                    subTask = subTask,
                    onToggle = { onToggleSubTask(subTask) },
                    onEdit = { onEditSubTask(subTask) },
                    onDelete = { onDeleteSubTask(subTask) }
                )
            }
        }
    }
}

/**
 * 子任务项
 */
@Composable
fun SubTaskItem(
    subTask: SubTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (subTask.isCompleted) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 复选框
            Checkbox(
                checked = subTask.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            // 内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subTask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (subTask.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (subTask.isCompleted) {
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    } else {
                        null
                    }
                )
                if (subTask.description.isNotEmpty()) {
                    Text(
                        text = subTask.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // 优先级标记
            PriorityDot(priority = subTask.priority)

            // 删除按钮
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除子任务",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // 编辑对话框
    if (showEditDialog) {
        SubTaskEditDialog(
            subTask = subTask,
            onDismiss = { showEditDialog = false },
            onSave = { updatedSubTask ->
                onEdit()
                // 实际保存由父组件处理
            }
        )
    }

    // 删除确认
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            title = { Text("删除子任务") },
            text = { Text("确定要删除这个子任务吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
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
            }
        )
    }
}

/**
 * 优先级指示点
 */
@Composable
private fun PriorityDot(priority: Priority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
        Priority.LOW -> MaterialTheme.colorScheme.outline
    }

    Spacer(modifier = Modifier.width(4.dp))
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
    Spacer(modifier = Modifier.width(8.dp))
}

/**
 * 子任务编辑对话框
 */
@Composable
fun SubTaskEditDialog(
    subTask: SubTask,
    onDismiss: () -> Unit,
    onSave: (SubTask) -> Unit
) {
    var title by remember { mutableStateOf(subTask.title) }
    var description by remember { mutableStateOf(subTask.description) }
    var priority by remember { mutableStateOf(subTask.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (subTask.id == 0L) Icons.Default.Add else Icons.Default.Edit,
                contentDescription = null
            )
        },
        title = { Text(if (subTask.id == 0L) "添加子任务" else "编辑子任务") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    placeholder = { Text("输入子任务标题") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    placeholder = { Text("添加详细描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // 优先级选择
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.entries.forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (p) {
                                        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                        Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                        Priority.LOW -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            subTask.copy(
                                title = title,
                                description = description,
                                priority = priority
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

/**
 * 添加子任务对话框
 */
@Composable
fun AddSubTaskDialog(
    parentTodoId: Long,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, priority: Priority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        title = { Text("添加子任务") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    placeholder = { Text("输入子任务标题") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    placeholder = { Text("添加详细描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // 优先级选择
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.entries.forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (p) {
                                        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                        Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                        Priority.LOW -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, priority)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
