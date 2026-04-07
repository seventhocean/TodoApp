package com.arttest.todo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 子任务实体
 */
@Entity(
    tableName = "sub_tasks",
    foreignKeys = [
        ForeignKey(
            entity = TodoItem::class,
            parentColumns = ["id"],
            childColumns = ["parentTodoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentTodoId")]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val parentTodoId: Long,           // 父任务 ID
    val title: String,                 // 子任务标题
    val description: String = "",      // 子任务描述（可选）
    val isCompleted: Boolean = false,  // 完成状态
    val priority: Priority = Priority.MEDIUM,  // 优先级
    val dueDate: java.time.LocalDate? = null,  // 截止日期
    val sortOrder: Int = 0             // 排序顺序
)
