package com.autotask

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autotask.ui.screens.SettingsScreen
import com.autotask.ui.screens.TaskEditScreen
import com.autotask.ui.screens.TaskListScreen
import com.autotask.ui.theme.AutoTaskTheme
import com.autotask.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            AutoTaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoTaskApp()
                }
            }
        }
    }
}

@Composable
fun AutoTaskApp() {
    val navController = rememberNavController()
    val viewModel: TaskViewModel = hiltViewModel()
    
    val tasks by viewModel.tasks.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val showTimePicker by viewModel.showTimePicker.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                tasks = tasks,
                onAddTask = {
                    viewModel.startEditing()
                    navController.navigate("task_edit")
                },
                onEditTask = { task ->
                    viewModel.startEditing(task)
                    navController.navigate("task_edit")
                },
                onToggleTask = { task ->
                    viewModel.toggleTaskEnabled(task)
                },
                onDeleteTask = { task ->
                    viewModel.deleteTask(task)
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("task_edit") {
            TaskEditScreen(
                state = editState,
                showTimePicker = showTimePicker,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onTimeClick = viewModel::showTimePicker,
                onTimeSelected = viewModel::updateTime,
                onTimeDismiss = viewModel::hideTimePicker,
                onRepeatTypeChange = viewModel::updateRepeatType,
                onRepeatDayToggle = viewModel::toggleRepeatDay,
                onActionTypeChange = viewModel::updateActionType,
                onActionDataChange = viewModel::updateActionData,
                onSave = {
                    viewModel.saveTask {
                        navController.popBackStack()
                    }
                },
                onBack = {
                    viewModel.resetEditState()
                    navController.popBackStack()
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
