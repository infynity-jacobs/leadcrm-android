package com.infynity.leadcrm.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.TaskCreateRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskCreateScreen(
    viewModel: TaskCreateViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var statusId by remember { mutableStateOf<Int?>(null) }
    var priority by remember { mutableStateOf("medium") }
    var taskType by remember { mutableStateOf("general") }
    var startDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var dueDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    LaunchedEffect(uiState.statuses) {
        if (statusId == null) statusId = uiState.statuses.firstOrNull()?.id
    }

    LaunchedEffect(uiState.saveSuccessful) {
        if (uiState.saveSuccessful) {
            viewModel.clearSaveSuccess()
            onBack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "New Task",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when {
            uiState.isLoadingStatuses && uiState.statuses.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.statuses.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "No active task statuses are available",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = viewModel::loadTaskStatuses) {
                        Text("Retry")
                    }
                    Button(onClick = onBack) {
                        Text("Cancel")
                    }
                }
            }

            else -> {
                TaskEditForm(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    statusId = statusId,
                    onStatusChange = { statusId = it },
                    statuses = uiState.statuses,
                    priority = priority,
                    onPriorityChange = { priority = it },
                    taskType = taskType,
                    onTaskTypeChange = { taskType = it },
                    startDateTime = startDateTime,
                    onStartDateTimeChange = { startDateTime = it },
                    dueDateTime = dueDateTime,
                    onDueDateTimeChange = { dueDateTime = it },
                    isSaving = uiState.isSaving,
                    errorMessage = uiState.errorMessage,
                    onCancel = onBack,
                    onSave = {
                        val selectedStatusId = statusId
                        if (selectedStatusId != null) {
                            viewModel.save(
                                TaskCreateRequest(
                                    title = title.trim(),
                                    statusId = selectedStatusId,
                                    description = description.trim().ifBlank { null },
                                    priority = priority,
                                    taskType = taskType,
                                    startDate = startDateTime?.atZone(
                                        java.time.ZoneId.systemDefault()
                                    )?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                    dueDate = dueDateTime?.atZone(
                                        java.time.ZoneId.systemDefault()
                                    )?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}
