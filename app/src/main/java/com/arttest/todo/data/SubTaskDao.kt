package com.arttest.todo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 子任务数据访问对象
 */
@Dao
interface SubTaskDao {

    // ============ 查询操作 ============

    /**
     * 获取单个子任务
     */
    @Query("SELECT * FROM sub_tasks WHERE id = :id")
    suspend fun getSubTaskById(id: Long): SubTask?

    /**
     * 获取父任务的所有子任务（按排序顺序）
     */
    @Query("SELECT * FROM sub_tasks WHERE parentTodoId = :parentTodoId ORDER BY sortOrder ASC, id ASC")
    fun getSubTasksByParentId(parentTodoId: Long): Flow<List<SubTask>>

    /**
     * 获取父任务的所有子任务（同步版本）
     */
    @Query("SELECT * FROM sub_tasks WHERE parentTodoId = :parentTodoId ORDER BY sortOrder ASC, id ASC")
    suspend fun getSubTasksByParentIdSync(parentTodoId: Long): List<SubTask>

    /**
     * 获取未完成的子任务数量
     */
    @Query("SELECT COUNT(*) FROM sub_tasks WHERE parentTodoId = :parentTodoId AND isCompleted = 0")
    suspend fun getIncompleteCount(parentTodoId: Long): Int

    // ============ 插入操作 ============

    /**
     * 插入子任务
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subTask: SubTask): Long

    // ============ 更新操作 ============

    /**
     * 更新子任务
     */
    @Update
    suspend fun update(subTask: SubTask)

    /**
     * 切换完成状态
     */
    @Query("UPDATE sub_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun toggleComplete(id: Long, isCompleted: Boolean)

    /**
     * 更新排序顺序
     */
    @Query("UPDATE sub_tasks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    // ============ 删除操作 ============

    /**
     * 删除子任务
     */
    @Delete
    suspend fun delete(subTask: SubTask)

    /**
     * 删除指定 ID 的子任务
     */
    @Query("DELETE FROM sub_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 删除父任务的所有子任务
     */
    @Query("DELETE FROM sub_tasks WHERE parentTodoId = :parentTodoId")
    suspend fun deleteAllByParentId(parentTodoId: Long)
}
