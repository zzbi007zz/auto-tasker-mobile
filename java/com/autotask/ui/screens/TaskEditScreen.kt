package com.autotask.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autotask.domain.model.ActionType
import com.autotask.domain.model.RepeatType
import com.autotask.viewmodel.TaskEditState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    state: TaskEditState,
    showTimePicker: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeClick: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onTimeDismiss: () -> Unit,
    onRepeatTypeChange: (RepeatType) -> Unit,
    onRepeatDayToggle: (Int) -> Unit,
    onActionTypeChange: (ActionType) -> Unit,
    onActionDataChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditing) "Edit Task" else "New Task",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = state.title.isNotBlank()
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Task Name
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Task Name") },
                placeholder = { Text("e.g., Morning Alarm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (optional)") },
                placeholder = { Text("Add a note...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // Time Picker
            SectionCard(
                title = "Time",
                icon = Icons.Default.Schedule
            ) {
                Surface(
                    onClick = onTimeClick,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d:%02d", state.hour, state.minute),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Repeat Type
            SectionCard(
                title = "Repeat",
                icon = Icons.Default.Repeat
            ) {
                Column {
                    RepeatType.entries.forEach { repeatType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = state.repeatType == repeatType,
                                    onClick = { onRepeatTypeChange(repeatType) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.repeatType == repeatType,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (repeatType) {
                                    RepeatType.ONCE -> "Once"
                                    RepeatType.DAILY -> "Every day"
                                    RepeatType.WEEKLY -> "Every week"
                                    RepeatType.CUSTOM -> "Custom days"
                                }
                            )
                        }
                    }

                    // Day selector for WEEKLY or CUSTOM
                    if (state.repeatType == RepeatType.WEEKLY || state.repeatType == RepeatType.CUSTOM) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DaySelector(
                            selectedDays = state.repeatDays,
                            onDayToggle = onRepeatDayToggle
                        )
                    }
                }
            }

            // Action Type
            SectionCard(
                title = "Action",
                icon = Icons.Default.PlayArrow
            ) {
                Column {
                    ActionType.entries.forEach { actionType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = state.actionType == actionType,
                                    onClick = { onActionTypeChange(actionType) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.actionType == actionType,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = when (actionType) {
                                    ActionType.NOTIFICATION -> Icons.Default.Notifications
                                    ActionType.OPEN_APP -> Icons.Default.PhoneAndroid
                                    ActionType.PLAY_SOUND -> Icons.Default.VolumeUp
                                    ActionType.GOOGLE_SHEET_COPY -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (actionType) {
                                    ActionType.NOTIFICATION -> "Show Notification"
                                    ActionType.OPEN_APP -> "Open App"
                                    ActionType.PLAY_SOUND -> "Play Alarm Sound"
                                    ActionType.GOOGLE_SHEET_COPY -> "Copy from Google Sheet"
                                }
                            )
                        }
                    }

                    // Package name input for OPEN_APP
                    if (state.actionType == ActionType.OPEN_APP) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.actionData,
                            onValueChange = onActionDataChange,
                            label = { Text("Package Name") },
                            placeholder = { Text("e.g., com.whatsapp") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (state.actionType == ActionType.GOOGLE_SHEET_COPY) {
                        val (sheetUrl, columnName) = remember(state.actionData) {
                            val parts = state.actionData.split(',')
                            (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = sheetUrl,
                            onValueChange = { onActionDataChange("$it,$columnName") },
                            label = { Text("Google Sheet URL") },
                            placeholder = { Text("Enter sheet URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = columnName,
                            onValueChange = { onActionDataChange("$sheetUrl,$it") },
                            label = { Text("Column Name") },
                            placeholder = { Text("e.g., A") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.hour,
            initialMinute = state.minute
        )
        
        AlertDialog(
            onDismissRequest = onTimeDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        onTimeDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onTimeDismiss) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        content()
    }
}

@Composable
private fun DaySelector(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            val dayNumber = index + 1
            val isSelected = selectedDays.contains(dayNumber)
            
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .toggleable(
                        value = isSelected,
                        onValueChange = { onDayToggle(dayNumber) },
                        role = Role.Checkbox
                    ),
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
