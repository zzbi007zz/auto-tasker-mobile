package com.autotask.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val triggerHour: Int,
    val triggerMinute: Int,
    val repeatType: String = "ONCE", // ONCE, DAILY, WEEKLY, CUSTOM
    val repeatDays: String = "", // Comma-separated days for WEEKLY/CUSTOM (e.g., "1,2,3" for Mon,Tue,Wed)
    val actionType: String = "NOTIFICATION", // NOTIFICATION, OPEN_APP, PLAY_SOUND
    val actionData: String = "", // Package name for OPEN_APP, sound URI for PLAY_SOUND
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null
)
