package com.autotask.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class RepeatType {
    ONCE,
    DAILY,
    WEEKLY,
    CUSTOM
}

enum class ActionType {
    NOTIFICATION,
    OPEN_APP,
    PLAY_SOUND
}

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val triggerHour: Int,
    val triggerMinute: Int,
    val repeatType: RepeatType = RepeatType.ONCE,
    val repeatDays: List<Int> = emptyList(), // 1=Monday, 7=Sunday
    val actionType: ActionType = ActionType.NOTIFICATION,
    val actionData: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null
) {
    val formattedTime: String
        get() {
            val time = LocalTime.of(triggerHour, triggerMinute)
            return time.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

    val repeatDescription: String
        get() = when (repeatType) {
            RepeatType.ONCE -> "Once"
            RepeatType.DAILY -> "Every day"
            RepeatType.WEEKLY -> "Every week"
            RepeatType.CUSTOM -> {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                repeatDays.mapNotNull { days.getOrNull(it - 1) }.joinToString(", ")
            }
        }
}
