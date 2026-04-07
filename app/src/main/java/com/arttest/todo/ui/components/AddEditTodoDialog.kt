package com.arttest.todo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arttest.todo.data.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 添加/编辑 Todo 对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoDialog(
    todo: TodoItem? = null,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(todo?.title ?: "") }
    var description by remember { mutableStateOf(todo?.description ?: "") }
    var priority by remember { mutableStateOf(todo?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(todo?.category ?: Category.OTHER) }
    var dueDate by remember { mutableStateOf(todo?.dueDate) }
    var hasReminder by remember { mutableStateOf(todo?.hasReminder ?: false) }
    var reminderTime by remember { mutableStateOf(todo?.reminderTime) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 标题
                Text(
                    text = if (todo == null) "新建待办" else "编辑待办",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    placeholder = { Text("输入待办事项标题") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 1
                )

                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    placeholder = { Text("添加更多细节") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 4
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
                            PrioritySelectionChip(
                                priority = p,
                                isSelected = priority == p,
                                onClick = { priority = p }
                            )
                        }
                    }
                }

                // 分类选择
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CategorySelector(
                        selectedCategory = category,
                        onCategorySelected = { category = it }
                    )
                }

                // 截止日期
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "截止日期",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dueDate?.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                                    ?: "选择日期"
                            )
                        }
                        if (dueDate != null) {
                            IconButton(onClick = { dueDate = null }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清除日期",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 提醒开关
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "设置提醒",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "到期时提醒我",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    // 提醒时间选择
                    if (hasReminder) {
                        OutlinedButton(
                            onClick = { showReminderTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reminderTime?.format(
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                                ) ?: "选择提醒时间"
                            )
                        }
                    }
                }

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val finalReminderTime = if (hasReminder) {
                                    reminderTime ?: dueDate?.atStartOfDay() ?: LocalDateTime.now().plusHours(1)
                                } else null
                                onSave(
                                    todo?.copy(
                                        title = title,
                                        description = description,
                                        priority = priority,
                                        category = category,
                                        dueDate = dueDate,
                                        hasReminder = hasReminder,
                                        reminderTime = finalReminderTime,
                                        updatedAt = LocalDateTime.now()
                                    ) ?: TodoItem(
                                        title = title,
                                        description = description,
                                        priority = priority,
                                        category = category,
                                        dueDate = dueDate,
                                        hasReminder = hasReminder,
                                        reminderTime = finalReminderTime
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Text(if (todo == null) "创建" else "保存")
                    }
                }
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = dueDate ?: LocalDate.now(),
            onDateSelected = { dueDate = it },
            onDismiss = { showDatePicker = false }
        )
    }

    // 提醒时间选择器
    if (showReminderTimePicker) {
        ReminderTimePickerDialog(
            selectedTime = reminderTime ?: (dueDate?.atStartOfDay() ?: LocalDateTime.now().plusHours(1)),
            onTimeSelected = { reminderTime = it },
            onDismiss = { showReminderTimePicker = false }
        )
    }
}

/**
 * 优先级选择 Chip
 */
@Composable
private fun PrioritySelectionChip(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        Priority.LOW -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(priority.name) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.first,
            selectedLabelColor = colors.second
        )
    )
}

/**
 * 日期选择器对话框
 */
@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var displayDate by remember { mutableStateOf(selectedDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 快速选择按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { displayDate = LocalDate.now() },
                        label = { Text("今天") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    AssistChip(
                        onClick = { displayDate = LocalDate.now().plusDays(1) },
                        label = { Text("明天") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    AssistChip(
                        onClick = { displayDate = LocalDate.now().plusWeeks(1) },
                        label = { Text("下周") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 当前选择的日期
                Text(
                    text = displayDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日")),
                    style = MaterialTheme.typography.titleMedium
                )

                // 简单的日期调整
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { displayDate = displayDate.minusDays(1) }) {
                        Icon(Icons.Default.ChevronLeft, "前一天")
                    }
                    IconButton(onClick = { displayDate = displayDate.plusDays(1) }) {
                        Icon(Icons.Default.ChevronRight, "后一天")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(displayDate)
                    onDismiss()
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("确定")
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
 * 提醒时间选择器对话框
 */
@Composable
private fun ReminderTimePickerDialog(
    selectedTime: LocalDateTime,
    onTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    var displayTime by remember { mutableStateOf(selectedTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择提醒时间") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 快速选择按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { displayTime = LocalDateTime.now().plusHours(1) },
                        label = { Text("1 小时后") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    AssistChip(
                        onClick = { displayTime = LocalDateTime.now().plusHours(2) },
                        label = { Text("2 小时后") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { displayTime = displayTime.minusMinutes(15) },
                        label = { Text("-15 分钟") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    AssistChip(
                        onClick = { displayTime = displayTime.plusMinutes(15) },
                        label = { Text("+15 分钟") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 当前选择的时间
                Text(
                    text = displayTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日 HH:mm")
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                // 时间调整
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayTime = displayTime.minusHours(1) }) {
                        Icon(Icons.Default.ChevronLeft, "前一小时")
                    }
                    Text(
                        text = displayTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { displayTime = displayTime.plusHours(1) }) {
                        Icon(Icons.Default.ChevronRight, "后一小时")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(displayTime)
                    onDismiss()
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("确定")
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
