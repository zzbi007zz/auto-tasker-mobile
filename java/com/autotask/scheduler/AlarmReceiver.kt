package com.autotask.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.autotask.MainActivity
import com.autotask.R
import com.autotask.data.database.AppDatabase
import com.autotask.domain.model.ActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        const val CHANNEL_ID = "autotask_notifications"
        const val CHANNEL_NAME = "AutoTask Notifications"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskScheduler.EXTRA_TASK_ID, -1)
        if (taskId == -1L) {
            Log.e(TAG, "Invalid task ID received")
            return
        }

        Log.d(TAG, "Alarm triggered for task: $taskId")
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val task = database.taskDao().getTaskById(taskId)
                
                if (task != null && task.isEnabled) {
                    val action = try {
                        ActionType.valueOf(task.actionType)
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG, "Unknown action type: ${task.actionType}")
                        ActionType.NOTIFICATION // Default to notification
                    }

                    // Execute the action
                    when (action) {
                        ActionType.NOTIFICATION -> showNotification(context, task.title, task.description)
                        ActionType.OPEN_APP -> openApp(context, task.actionData)
                        ActionType.PLAY_SOUND -> playSound(context, task.title)
                    }
                    
                    // Update last triggered time
                    database.taskDao().updateLastTriggered(taskId, System.currentTimeMillis())
                    
                    // Reschedule if it's a repeating task
                    if (task.repeatType != "ONCE") {
                        val scheduler = TaskScheduler(context, 
                            com.autotask.data.repository.TaskRepository(database.taskDao()))
                        scheduler.scheduleTask(task)
                    } else {
                        // Disable one-time task after execution
                        database.taskDao().setTaskEnabled(taskId, false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing alarm: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "AutoTask scheduled task notifications"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message.ifBlank { "Scheduled task completed" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d(TAG, "Notification shown: $title")
    }

    private fun openApp(context: Context, packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Log.d(TAG, "Opened app: $packageName")
            } else {
                Log.e(TAG, "App not found: $packageName")
                showNotification(context, "AutoTask", "Could not open app: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app: ${e.message}")
        }
    }

    private fun playSound(context: Context, title: String) {
        try {
            val ringtone = RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
            ringtone?.play()
            showNotification(context, title, "Alarm triggered - tap to dismiss")
            Log.d(TAG, "Playing alarm sound for: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }
}
