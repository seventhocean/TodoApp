package com.arttest.todo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Todo 数据访问对象
 */
@Dao
interface TodoDao {

    // ============ 查询操作 ============

    /**
     * 获取所有 Todo 项（按创建时间倒序）
     */
    @Query("SELECT * FROM todo_items ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoItem>>

    /**
     * 获取未完成的 Todo
     */
    @Query("SELECT * FROM todo_items WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getActiveTodos(): Flow<List<TodoItem>>

    /**
     * 获取已完成的 Todo
     */
    @Query("SELECT * FROM todo_items WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedTodos(): Flow<List<TodoItem>>

    /**
     * 按优先级筛选
     */
    @Query("SELECT * FROM todo_items WHERE priority = :priority ORDER BY createdAt DESC")
    fun getTodosByPriority(priority: Priority): Flow<List<TodoItem>>

    /**
     * 按分类筛选
     */
    @Query("SELECT * FROM todo_items WHERE category = :category ORDER BY createdAt DESC")
    fun getTodosByCategory(category: Category): Flow<List<TodoItem>>

    /**
     * 搜索标题和描述
     */
    @Query("SELECT * FROM todo_items WHERE title LIKE :query OR description LIKE :query ORDER BY createdAt DESC")
    fun searchTodos(query: String): Flow<List<TodoItem>>

    /**
     * 获取今天到期的 Todo
     */
    @Query("SELECT * FROM todo_items WHERE dueDate = :date AND isCompleted = 0")
    fun getTodosDueToday(date: LocalDate): Flow<List<TodoItem>>

    /**
     * 获取即将过期的 Todo（3 天内）
     */
    @Query("SELECT * FROM todo_items WHERE dueDate BETWEEN date('now') AND date('now', '+3 days') AND isCompleted = 0")
    fun getTodosDueSoon(): Flow<List<TodoItem>>

    /**
     * 获取单个 Todo
     */
    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoItem?

    // ============ 插入操作 ============

    /**
     * 插入单个 Todo
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem): Long

    /**
     * 插入多个 Todo
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(todos: List<TodoItem>)

    // ============ 更新操作 ============

    /**
     * 更新 Todo
     */
    @Update
    suspend fun update(todo: TodoItem)

    /**
     * 切换完成状态
     */
    @Query("UPDATE todo_items SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleComplete(id: Long, isCompleted: Boolean, updatedAt: java.time.LocalDateTime)

    // ============ 删除操作 ============

    /**
     * 删除单个 Todo
     */
    @Delete
    suspend fun delete(todo: TodoItem)

    /**
     * 删除指定 ID 的 Todo
     */
    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 删除所有已完成的 Todo
     */
    @Query("DELETE FROM todo_items WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    /**
     * 删除所有 Todo
     */
    @Query("DELETE FROM todo_items")
    suspend fun deleteAll()

    // ============ 统计操作 ============

    /**
     * 获取总数
     */
    @Query("SELECT COUNT(*) FROM todo_items")
    fun getTotalCount(): Flow<Int>

    /**
     * 获取未完成数量
     */
    @Query("SELECT COUNT(*) FROM todo_items WHERE isCompleted = 0")
    fun getActiveCount(): Flow<Int>

    /**
     * 获取已完成数量
     */
    @Query("SELECT COUNT(*) FROM todo_items WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>
}
