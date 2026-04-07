package com.arttest.todo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arttest.todo.data.Priority

/**
 * 优先级 Chip 组件 - Material You 风格
 */
@Composable
fun PriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (priority) {
        Priority.HIGH -> {
            Triple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                "高"
            )
        }
        Priority.MEDIUM -> {
            Triple(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
                "中"
            )
        }
        Priority.LOW -> {
            Triple(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurfaceVariant,
                "低"
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = backgroundColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 优先级指示点
        Box(
            modifier = Modifier
                .padding(start = 6.dp, end = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(
                    width = 8.dp,
                    color = backgroundColor,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

/**
 * 获取优先色的颜色
 */
fun Priority.getColor(): Color = when (this) {
    Priority.HIGH -> Color(0xFFB3261E)
    Priority.MEDIUM -> Color(0xFF625B71)
    Priority.LOW -> Color(0xFF006D1C)
}
