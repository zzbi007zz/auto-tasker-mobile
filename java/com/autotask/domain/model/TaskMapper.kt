package com.autotask.domain.model

import com.autotask.data.database.TaskEntity

fun TaskEntity.toTask(): Task = Task(
    id = id,
    title = title,
    description = description,
    triggerHour = triggerHour,
    triggerMinute = triggerMinute,
    repeatType = RepeatType.valueOf(repeatType),
    repeatDays = if (repeatDays.isBlank()) emptyList() else repeatDays.split(",").map { it.toInt() },
    actionType = ActionType.valueOf(actionType),
    actionData = actionData,
    isEnabled = isEnabled,
    createdAt = createdAt,
    lastTriggered = lastTriggered
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    triggerHour = triggerHour,
    triggerMinute = triggerMinute,
    repeatType = repeatType.name,
    repeatDays = repeatDays.joinToString(","),
    actionType = actionType.name,
    actionData = actionData,
    isEnabled = isEnabled,
    createdAt = createdAt,
    lastTriggered = lastTriggered
)
