package com.infynity.leadcrm.feature.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.TaskUpdateRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun TaskEditScreen(
    viewModel: TaskEditViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var initializedTaskId by remember { mutableStateOf<Int?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var statusId by remember { mutableStateOf<Int?>(null) }
    var priority by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var dueDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    LaunchedEffect(uiState.task?.id) {
        val task = uiState.task ?: return@LaunchedEffect

        if (initializedTaskId != task.id) {
            title = task.title
            description = task.description.orEmpty()
            statusId = task.statusId
            priority = task.priority
            taskType = task.taskType
            startDateTime = parseTaskDateTime(task.startDate)
            dueDateTime = parseTaskDateTime(task.dueDate)
            initializedTaskId = task.id
        }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Task",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
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

                    Button(onClick = viewModel::loadTask) {
                        Text("Retry")
                    }
                }
            }

            uiState.task != null -> {
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
                        val start = startDateTime
                        val due = dueDateTime

                        if (start == null ||
                            due == null ||
                            !due.isBefore(start)
                        ) {
                            viewModel.save(
                                TaskUpdateRequest(
                                    title = title.trim(),
                                    description = description.trim().ifBlank { null },
                                    statusId = statusId,
                                    priority = priority.ifBlank { null },
                                    taskType = taskType.ifBlank { null },
                                    startDate = start?.atZone(
                                        java.time.ZoneId.systemDefault()
                                    )?.format(
                                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                    ),
                                    dueDate = due?.atZone(
                                        java.time.ZoneId.systemDefault()
                                    )?.format(
                                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                    )
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskEditForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    statusId: Int?,
    onStatusChange: (Int?) -> Unit,
    statuses: List<com.infynity.leadcrm.core.network.models.TaskStatusResponse>,
    priority: String,
    onPriorityChange: (String) -> Unit,
    taskType: String,
    onTaskTypeChange: (String) -> Unit,
    startDateTime: LocalDateTime?,
    onStartDateTimeChange: (LocalDateTime) -> Unit,
    dueDateTime: LocalDateTime?,
    onDueDateTimeChange: (LocalDateTime) -> Unit,
    isSaving: Boolean,
    errorMessage: String?,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    LazyTaskColumn {
        item {
            TaskEditSectionCard(title = "Task") {
                TaskEditTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = "Title",
                    enabled = !isSaving,
                    required = true
                )

                val keyboardController =
                    LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    label = { Text("Description") },
                    minLines = 4,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    enabled = !isSaving
                )
            }
        }

        item {
            TaskEditSectionCard(title = "Status") {
                if (statuses.isEmpty()) {
                    Text(
                        text = "No active statuses available.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    TaskFilterRow {
                        statuses.forEach { status ->
                            FilterChip(
                                selected = statusId == status.id,
                                onClick = {
                                    if (!isSaving) {
                                        onStatusChange(status.id)
                                    }
                                },
                                label = { Text(status.name) },
                                enabled = !isSaving
                            )
                        }
                    }
                }
            }
        }

        item {
            TaskEditSectionCard(title = "Priority") {
                TaskFilterRow {
                    listOf("low", "medium", "high", "urgent")
                        .forEach { value ->
                            FilterChip(
                                selected = priority.equals(
                                    value,
                                    ignoreCase = true
                                ),
                                onClick = {
                                    if (!isSaving) {
                                        onPriorityChange(value)
                                    }
                                },
                                label = {
                                    Text(
                                        value.replaceFirstChar {
                                            it.uppercase()
                                        }
                                    )
                                },
                                enabled = !isSaving
                            )
                        }
                }
            }
        }

        item {
            TaskEditSectionCard(title = "Task Type") {
                TaskFilterRow {
                    listOf(
                        "follow_up",
                        "call",
                        "meeting",
                        "site_visit",
                        "document",
                        "installation",
                        "payment",
                        "general"
                    ).forEach { value ->
                        FilterChip(
                            selected = taskType.equals(
                                value,
                                ignoreCase = true
                            ),
                            onClick = {
                                if (!isSaving) {
                                    onTaskTypeChange(value)
                                }
                            },
                            label = {
                                Text(formatTaskType(value))
                            },
                            enabled = !isSaving
                        )
                    }
                }
            }
        }

        item {
            TaskEditSectionCard(title = "Schedule") {
                TaskDateTimeField(
                    label = "Start Date",
                    value = startDateTime,
                    enabled = !isSaving,
                    onValueChange = onStartDateTimeChange
                )

                TaskDateTimeField(
                    label = "Due Date",
                    value = dueDateTime,
                    enabled = !isSaving,
                    onValueChange = onDueDateTimeChange
                )

                if (startDateTime != null &&
                    dueDateTime != null &&
                    dueDateTime.isBefore(startDateTime)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Due date/time cannot be earlier than the start date/time.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving &&
                        title.isNotBlank() &&
                        !(startDateTime != null &&
                            dueDateTime != null &&
                            dueDateTime.isBefore(startDateTime))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp)
                        )
                    } else {
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskFilterRow(
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
private fun LazyTaskColumn(
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun TaskDateTimeField(
    label: String,
    value: LocalDateTime?,
    enabled: Boolean,
    onValueChange: (LocalDateTime) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a")

    fun showPicker() {
        val initial = value ?: LocalDateTime.now()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onValueChange(
                            LocalDateTime.of(
                                year,
                                month + 1,
                                dayOfMonth,
                                hour,
                                minute
                            )
                        )
                    },
                    initial.hour,
                    initial.minute,
                    false
                ).show()
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    OutlinedButton(
        onClick = { showPicker() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value?.format(displayFormatter)
                        ?: "Select date and time",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "Select",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TaskEditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    required: Boolean = false
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = {
            Text(if (required) "$label *" else label)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        ),
        enabled = enabled
    )
}

@Composable
private fun TaskEditSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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

private fun formatTaskType(value: String): String {
    return value
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}


private fun parseTaskDateTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null

    val trimmed = value.trim()

    val dateTimeFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm")
    )

    for (formatter in dateTimeFormatters) {
        try {
            return LocalDateTime.parse(trimmed, formatter)
        } catch (_: DateTimeParseException) {
        }
    }

    val dateFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )

    for (formatter in dateFormatters) {
        try {
            return LocalDate.parse(trimmed, formatter).atStartOfDay()
        } catch (_: DateTimeParseException) {
        }
    }

    return try {
        java.time.OffsetDateTime.parse(trimmed)
            .atZoneSameInstant(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
    } catch (_: DateTimeParseException) {
        null
    }
}
