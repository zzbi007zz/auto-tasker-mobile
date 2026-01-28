package com.autotask.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.autotask.data.database.TaskEntity
import com.autotask.data.repository.TaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "TaskScheduler"
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    fun scheduleTask(task: TaskEntity) {
        if (!task.isEnabled) {
            cancelTask(task.id)
            return
        }

        val triggerTime = calculateNextTriggerTime(task)
        val pendingIntent = createPendingIntent(task.id)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled task ${task.id} for $triggerTime")
                } else {
                    Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled task ${task.id} for $triggerTime")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
        }
    }

    fun cancelTask(taskId: Long) {
        val pendingIntent = createPendingIntent(taskId)
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled task $taskId")
    }

    fun rescheduleAllTasks() {
        scope.launch {
            val tasks = taskRepository.getEnabledTasksList()
            tasks.forEach { task ->
                scheduleTask(task)
            }
            Log.d(TAG, "Rescheduled ${tasks.size} tasks")
        }
    }

    private fun calculateNextTriggerTime(task: TaskEntity): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, task.triggerHour)
            set(Calendar.MINUTE, task.triggerMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, schedule for tomorrow (for ONCE and DAILY)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            when (task.repeatType) {
                "ONCE", "DAILY" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                "WEEKLY", "CUSTOM" -> {
                    // Find the next valid day
                    val repeatDays = if (task.repeatDays.isBlank()) {
                        listOf(calendar.get(Calendar.DAY_OF_WEEK))
                    } else {
                        task.repeatDays.split(",").map { it.toInt() }
                    }
                    
                    var daysToAdd = 1
                    while (daysToAdd <= 7) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        // Convert Calendar.DAY_OF_WEEK (1=Sunday) to our format (1=Monday)
                        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.MONDAY -> 1
                            Calendar.TUESDAY -> 2
                            Calendar.WEDNESDAY -> 3
                            Calendar.THURSDAY -> 4
                            Calendar.FRIDAY -> 5
                            Calendar.SATURDAY -> 6
                            Calendar.SUNDAY -> 7
                            else -> 1
                        }
                        if (repeatDays.contains(dayOfWeek)) break
                        daysToAdd++
                    }
                }
            }
        }

        return calendar.timeInMillis
    }

    private fun createPendingIntent(taskId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
