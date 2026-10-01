package com.infynity.leadcrm.feature.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val taskDetailDateFormatter = DateTimeFormatter.ofPattern(
    "dd MMM yyyy, h:mm a"
)

@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    canDelete: Boolean,
    onDeleteSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteSuccessful) {
        if (uiState.deleteSuccessful) {
            viewModel.clearDeleteSuccess()
            onDeleteSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Task Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit task"
                )
            }

            if (canDelete) {
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete task"
                        )
                    }
                }
            }

            IconButton(onClick = viewModel::refresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh task"
                )
            }
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.isDeleting) {
                        showDeleteConfirmation = false
                    }
                },
                title = {
                    Text("Delete Task")
                },
                text = {
                    Text(
                        "Are you sure you want to delete this task? " +
                            "This action cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            viewModel.deleteTask()
                        },
                        enabled = !uiState.isDeleting
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false },
                        enabled = !uiState.isDeleting
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        when {
            uiState.isLoading && uiState.task == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.task == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage
                            ?: "Unable to load task",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = viewModel::refresh) {
                        Text("Retry")
                    }
                }
            }

            uiState.task != null -> {
                TaskDetailContent(task = uiState.task!!)
            }
        }
    }
}

@Composable
private fun TaskDetailContent(
    task: TaskResponse
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TaskSectionCard(title = "Task") {
                TaskInlineDetailText("Title", task.title)
                TaskInlineDetailText("Description", task.description)
                TaskInlineDetailText("Status", task.statusName)
                TaskInlineDetailText(
                    "Priority",
                    task.priority.replaceFirstChar { it.uppercase() }
                )
                TaskInlineDetailText(
                    "Task Type",
                    formatTaskType(task.taskType)
                )
            }
        }

        item {
            TaskSectionCard(title = "Schedule") {
                DetailDateText("Start Date", task.startDate)
                DetailDateText("Due Date", task.dueDate)
                DetailDateText("Completed", task.completedAt)
            }
        }

        if (!task.leadName.isNullOrBlank() || task.leadId != null) {
            item {
                TaskSectionCard(title = "Lead") {
                    TaskInlineDetailText("Lead", task.leadName)
                    task.leadId?.let {
                        TaskInlineDetailText("Lead ID", it.toString())
                    }
                }
            }
        }

        if (!task.assignedToName.isNullOrBlank() ||
            !task.teamName.isNullOrBlank() ||
            !task.createdByName.isNullOrBlank()
        ) {
            item {
                TaskSectionCard(title = "Assignment") {
                    TaskInlineDetailText("Assigned To", task.assignedToName)
                    TaskInlineDetailText("Team", task.teamName)
                    TaskInlineDetailText("Created By", task.createdByName)
                }
            }
        }

        item {
            TaskSectionCard(title = "Record") {
                DetailDateText("Created", task.createdAt)
                DetailDateText("Last Updated", task.updatedAt)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TaskSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
private fun TaskInlineDetailText(
    label: String,
    value: String?
) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailText(
    label: String,
    value: String?
) {
    if (!value.isNullOrBlank()) {
        Column(
            modifier = Modifier.padding(vertical = 3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DetailDateText(
    label: String,
    value: String?
) {
    if (!value.isNullOrBlank()) {
        DetailText(
            label = label,
            value = formatTaskDate(value)
        )
    }
}

private fun formatTaskType(value: String): String {
    return value
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}

private fun formatTaskDate(value: String): String {
    return try {
        Instant.parse(value)
            .atZone(ZoneId.systemDefault())
            .format(taskDetailDateFormatter)
    } catch (_: Exception) {
        value
    }
}
