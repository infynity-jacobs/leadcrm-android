package com.infynity.leadcrm.feature.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val taskDateFormatter = DateTimeFormatter.ofPattern(
    "dd MMM yyyy, h:mm a"
)

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onTaskSelected: (Int) -> Unit,
    onNewTask: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val visibleTasks = viewModel.visibleTasks(uiState)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onNewTask) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New task"
                    )
                }

                IconButton(onClick = viewModel::refresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh tasks"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search tasks...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterRow {
                FilterChip(
                    selected = uiState.selectedStatusId == null,
                    onClick = { viewModel.setStatus(null) },
                    label = { Text("All") }
                )

                uiState.statuses.forEach { status ->
                    FilterChip(
                        selected = uiState.selectedStatusId == status.id,
                        onClick = { viewModel.setStatus(status.id) },
                        label = { Text(status.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilterRow {
                FilterChip(
                    selected = uiState.selectedPriority == null,
                    onClick = { viewModel.setPriority(null) },
                    label = { Text("All priorities") }
                )

                listOf("low", "medium", "high", "urgent").forEach { priority ->
                    FilterChip(
                        selected = uiState.selectedPriority == priority,
                        onClick = { viewModel.setPriority(priority) },
                        label = {
                            Text(priority.replaceFirstChar { it.uppercase() })
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilterRow {
                FilterChip(
                    selected = uiState.selectedTaskType == null,
                    onClick = { viewModel.setTaskType(null) },
                    label = { Text("All types") }
                )

                listOf(
                    "follow_up",
                    "call",
                    "meeting",
                    "site_visit",
                    "document",
                    "installation",
                    "payment",
                    "general"
                ).forEach { taskType ->
                    FilterChip(
                        selected = uiState.selectedTaskType == taskType,
                        onClick = { viewModel.setTaskType(taskType) },
                        label = {
                            Text(formatTaskType(taskType))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading && uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null && uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage
                                ?: "Unable to load tasks",
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = viewModel::refresh) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                Text(
                    text = "${visibleTasks.size} tasks",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (visibleTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tasks match the current filters.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = visibleTasks,
                            key = { it.id }
                        ) { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskSelected(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun TaskCard(
    task: TaskResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            task.leadName?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Lead: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatTaskType(task.taskType),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = task.priority.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium
                )
            }

            task.statusName?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            task.dueDate?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due: ${formatTaskDate(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            task.assignedToName?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Assigned: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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
            .format(taskDateFormatter)
    } catch (_: Exception) {
        value
    }
}
