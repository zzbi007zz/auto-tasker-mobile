package com.autotask.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autotask.data.database.AppDatabase
import com.autotask.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Boot completed - rescheduling all tasks")
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val repository = TaskRepository(database.taskDao())
                    val scheduler = TaskScheduler(context, repository)
                    scheduler.rescheduleAllTasks()
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling tasks: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
