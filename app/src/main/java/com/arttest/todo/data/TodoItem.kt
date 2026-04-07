package com.arttest.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 优先级枚举
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * 分类枚举
 */
enum class Category(val displayName: String) {
    WORK("工作"),
    PERSONAL("个人"),
    SHOPPING("购物"),
    HEALTH("健康"),
    STUDY("学习"),
    OTHER("其他")
}

/**
 * 重复类型枚举
 */
enum class RepeatType(val displayName: String) {
    NONE("不重复"),
    DAILY("每天"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年")
}

/**
 * Todo 数据实体
 */
@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,                    // 标题
    val description: String = "",         // 描述（可选）
    val isCompleted: Boolean = false,     // 完成状态

    val priority: Priority = Priority.MEDIUM,  // 优先级
    val category: Category = Category.OTHER,   // 分类

    val dueDate: LocalDate? = null,       // 截止日期
    val createdAt: LocalDateTime = LocalDateTime.now(),  // 创建时间
    val updatedAt: LocalDateTime = LocalDateTime.now(),  // 更新时间

    val hasReminder: Boolean = false,     // 是否有提醒
    val reminderTime: LocalDateTime? = null,  // 提醒时间

    val repeatType: RepeatType = RepeatType.NONE,  // 重复类型
    val repeatEndDate: LocalDate? = null  // 重复结束日期
)
