package com.arttest.todo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arttest.todo.data.TodoItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Todo 列表项卡片 - Material You 风格
 */
@Composable
fun TodoItemCard(
    todo: TodoItem,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 复选框
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 内容区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 标题
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (todo.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (todo.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 描述（如果有）
                if (todo.description.isNotEmpty()) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 元数据行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 优先级
                    PriorityChip(priority = todo.priority)

                    // 分类
                    CategoryChip(category = todo.category)

                    // 截止日期
                    todo.dueDate?.let { date ->
                        DueDateBadge(dueDate = date)
                    }
                }
            }
        }
    }
}

/**
 * 截止日期徽章
 */
@Composable
fun DueDateBadge(
    dueDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, dueDate)

    val (backgroundColor, textColor, text) = when {
        daysUntil < 0 -> {
            // 已过期
            Triple(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.onErrorContainer,
                "已过期"
            )
        }
        daysUntil == 0L -> {
            // 今天
            Triple(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.onPrimaryContainer,
                "今天"
            )
        }
        daysUntil == 1L -> {
            // 明天
            Triple(
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.onTertiaryContainer,
                "明天"
            )
        }
        daysUntil <= 7 -> {
            // 7 天内
            Triple(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.onSecondaryContainer,
                "${daysUntil}天后"
            )
        }
        else -> {
            // 超过 7 天
            Triple(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurfaceVariant,
                dueDate.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd"))
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 小圆点指示器
        Canvas(
            modifier = Modifier.size(6.dp)
        ) {
            drawCircle(
                color = textColor,
                radius = size.minDimension / 2
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * 空状态组件
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 装饰性插图
        Text(
            text = "📝",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
