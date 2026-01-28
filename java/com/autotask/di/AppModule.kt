package com.autotask.di

import android.content.Context
import com.autotask.data.database.AppDatabase
import com.autotask.data.database.TaskDao
import com.autotask.data.repository.TaskRepository
import com.autotask.scheduler.TaskScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepository(taskDao)
    }

    @Provides
    @Singleton
    fun provideTaskScheduler(
        @ApplicationContext context: Context,
        taskRepository: TaskRepository
    ): TaskScheduler {
        return TaskScheduler(context, taskRepository)
    }
}
