package com.arttest.todo.notification

import android.content.Context
import androidx.work.*
import com.arttest.todo.data.TodoDatabase
import com.arttest.todo.viewmodel.TodoViewModel
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 提醒通知 Worker
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val todoId = inputData.getLong("todo_id", -1)

            if (todoId == -1L) {
                return Result.failure()
            }

            // 获取数据库和待办项
            val database = TodoDatabase.getDatabase(applicationContext)
            val todo = database.todoDao().getTodoById(todoId)

            if (todo != null && !todo.isCompleted) {
                // 显示通知
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showNotification(
                    todoId = todo.id,
                    title = todo.title,
                    description = todo.description
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        /**
         * 设置一次性提醒
         */
        fun scheduleOneTimeReminder(
            context: Context,
            todoId: Long,
            delayMillis: Long
        ) {
            val constraints = Constraints.Builder().build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(
                    workDataOf(
                        "todo_id" to todoId
                    )
                )
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        /**
         * 设置周期性提醒
         */
        fun schedulePeriodicReminder(
            context: Context,
            todoId: Long,
            intervalHours: Long = 24
        ) {
            val constraints = Constraints.Builder().build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                intervalHours,
                TimeUnit.HOURS
            )
                .setInputData(
                    workDataOf(
                        "todo_id" to todoId
                    )
                )
                .setInitialDelay(intervalHours, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        /**
         * 取消提醒
         */
        fun cancelReminder(context: Context, todoId: Long) {
            WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$todoId")
        }

        /**
         * 取消所有提醒
         */
        fun cancelAllReminders(context: Context) {
            WorkManager.getInstance(context).cancelAllWork()
        }
    }
}
