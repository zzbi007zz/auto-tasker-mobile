package com.autotask.data.repository

import com.autotask.data.database.TaskDao
import com.autotask.data.database.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getEnabledTasks(): Flow<List<TaskEntity>> = taskDao.getEnabledTasks()

    suspend fun getEnabledTasksList(): List<TaskEntity> = taskDao.getEnabledTasksList()

    suspend fun getTaskById(taskId: Long): TaskEntity? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(taskId: Long) = taskDao.deleteTaskById(taskId)

    suspend fun setTaskEnabled(taskId: Long, isEnabled: Boolean) = 
        taskDao.setTaskEnabled(taskId, isEnabled)

    suspend fun updateLastTriggered(taskId: Long, timestamp: Long) =
        taskDao.updateLastTriggered(taskId, timestamp)
}
