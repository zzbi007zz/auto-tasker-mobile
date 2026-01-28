package com.autotask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autotask.data.database.TaskEntity
import com.autotask.data.repository.TaskRepository
import com.autotask.domain.model.ActionType
import com.autotask.domain.model.RepeatType
import com.autotask.domain.model.Task
import com.autotask.domain.model.toEntity
import com.autotask.domain.model.toTask
import com.autotask.scheduler.TaskScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskEditState(
    val title: String = "",
    val description: String = "",
    val hour: Int = 8,
    val minute: Int = 0,
    val repeatType: RepeatType = RepeatType.ONCE,
    val repeatDays: List<Int> = emptyList(),
    val actionType: ActionType = ActionType.NOTIFICATION,
    val actionData: String = "",
    val isEditing: Boolean = false,
    val editingTaskId: Long? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val scheduler: TaskScheduler
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .map { entities -> entities.map { it.toTask() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editState = MutableStateFlow(TaskEditState())
    val editState: StateFlow<TaskEditState> = _editState.asStateFlow()

    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()

    fun updateTitle(title: String) {
        _editState.value = _editState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _editState.value = _editState.value.copy(description = description)
    }

    fun updateTime(hour: Int, minute: Int) {
        _editState.value = _editState.value.copy(hour = hour, minute = minute)
    }

    fun updateRepeatType(repeatType: RepeatType) {
        _editState.value = _editState.value.copy(repeatType = repeatType)
    }

    fun toggleRepeatDay(day: Int) {
        val currentDays = _editState.value.repeatDays.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _editState.value = _editState.value.copy(repeatDays = currentDays.sorted())
    }

    fun updateActionType(actionType: ActionType) {
        _editState.value = _editState.value.copy(actionType = actionType)
    }

    fun updateActionData(actionData: String) {
        _editState.value = _editState.value.copy(actionData = actionData)
    }

    fun showTimePicker() {
        _showTimePicker.value = true
    }

    fun hideTimePicker() {
        _showTimePicker.value = false
    }

    fun startEditing(task: Task? = null) {
        _editState.value = if (task != null) {
            TaskEditState(
                title = task.title,
                description = task.description,
                hour = task.triggerHour,
                minute = task.triggerMinute,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays,
                actionType = task.actionType,
                actionData = task.actionData,
                isEditing = true,
                editingTaskId = task.id
            )
        } else {
            TaskEditState()
        }
    }

    fun resetEditState() {
        _editState.value = TaskEditState()
    }

    fun saveTask(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _editState.value
            val task = Task(
                id = state.editingTaskId ?: 0,
                title = state.title.ifBlank { "Untitled Task" },
                description = state.description,
                triggerHour = state.hour,
                triggerMinute = state.minute,
                repeatType = state.repeatType,
                repeatDays = state.repeatDays,
                actionType = state.actionType,
                actionData = state.actionData,
                isEnabled = true
            )
            
            val entity = task.toEntity()
            val savedId = if (state.editingTaskId != null) {
                repository.updateTask(entity)
                state.editingTaskId
            } else {
                repository.insertTask(entity)
            }
            
            // Schedule the task
            val savedTask = repository.getTaskById(savedId)
            savedTask?.let { scheduler.scheduleTask(it) }
            
            resetEditState()
            onComplete()
        }
    }

    fun toggleTaskEnabled(task: Task) {
        viewModelScope.launch {
            val newEnabled = !task.isEnabled
            repository.setTaskEnabled(task.id, newEnabled)
            
            val updatedTask = repository.getTaskById(task.id)
            updatedTask?.let {
                if (newEnabled) {
                    scheduler.scheduleTask(it)
                } else {
                    scheduler.cancelTask(task.id)
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            scheduler.cancelTask(task.id)
            repository.deleteTaskById(task.id)
        }
    }
}
