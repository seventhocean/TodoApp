package com.arttest.todo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.arttest.todo.data.Category

/**
 * 分类 Chip 组件 - Material You 风格
 */
@Composable
fun CategoryChip(
    category: Category,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Text(
                text = category.getIcon(),
                modifier = Modifier.padding(start = 6.dp, end = 2.dp)
            )
        }
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

/**
 * 分类对应的 Emoji 图标
 */
fun Category.getIcon(): String = when (this) {
    Category.WORK -> "💼"
    Category.PERSONAL -> "👤"
    Category.SHOPPING -> "🛒"
    Category.HEALTH -> "💪"
    Category.STUDY -> "📚"
    Category.OTHER -> "📌"
}
