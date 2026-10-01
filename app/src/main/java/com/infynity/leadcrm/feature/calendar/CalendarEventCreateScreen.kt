package com.infynity.leadcrm.feature.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infynity.leadcrm.core.network.models.CalendarEventCreateRequest
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val createEventTypes = listOf(
    "general",
    "meeting",
    "call",
    "follow_up",
    "site_visit",
    "installation",
    "payment"
)

@Composable
fun CalendarEventCreateScreen(
    viewModel: CalendarEventCreateViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("general") }
    var startDateTime by remember {
        mutableStateOf(LocalDateTime.now().withSecond(0).withNano(0))
    }
    var endDateTime by remember {
        mutableStateOf(
            LocalDateTime.now()
                .withSecond(0)
                .withNano(0)
                .plusHours(1)
        )
    }
    var allDay by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }

    var selectedLead by remember { mutableStateOf<LeadResponse?>(null) }
    var leadSearchQuery by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<TaskResponse?>(null) }
    var taskSearchQuery by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.saveSuccessful) {
        if (state.saveSuccessful) {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "New Calendar Event",
            style = MaterialTheme.typography.headlineSmall
        )

        CalendarCreateSectionCard(title = "Event") {
            CalendarCreateTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = "Title",
                enabled = !state.isSaving
            )

            CalendarCreateTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                enabled = !state.isSaving,
                singleLine = false,
                minLines = 3
            )
        }

        CalendarCreateSectionCard(title = "Event Type") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                createEventTypes.take(4).forEach { type ->
                    FilterChip(
                        selected = eventType == type,
                        onClick = { eventType = type },
                        label = { Text(type.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                        enabled = !state.isSaving
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                createEventTypes.drop(4).forEach { type ->
                    FilterChip(
                        selected = eventType == type,
                        onClick = { eventType = type },
                        label = { Text(type.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                        enabled = !state.isSaving
                    )
                }
            }
        }

        CalendarCreateSectionCard(title = "Lead") {
            if (selectedLead == null) {
                OutlinedTextField(
                    value = leadSearchQuery,
                    onValueChange = {
                        leadSearchQuery = it
                        viewModel.searchLeads(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lead") },
                    placeholder = { Text("Search for a lead") },
                    enabled = !state.isSaving,
                    singleLine = true
                )

                if (state.isSearchingLeads) {
                    CircularProgressIndicator()
                }

                state.leadSearchError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.leadSearchResults.forEach { lead ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLead = lead
                                leadSearchQuery = ""
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = listOfNotNull(
                                    lead.firstName,
                                    lead.lastName
                                ).joinToString(" "),
                                style = MaterialTheme.typography.titleSmall
                            )

                            lead.company?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            lead.phone?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                if (!state.isSearchingLeads &&
                    leadSearchQuery.isNotBlank() &&
                    state.leadSearchResults.isEmpty() &&
                    state.leadSearchError == null
                ) {
                    Text("No leads found.")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = listOfNotNull(
                            selectedLead?.firstName,
                            selectedLead?.lastName
                        ).joinToString(" "),
                        style = MaterialTheme.typography.titleSmall
                    )

                    selectedLead?.company?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    selectedLead?.phone?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                selectedLead = null
                                leadSearchQuery = ""
                                viewModel.searchLeads("")
                            },
                            enabled = !state.isSaving
                        ) {
                            Text("Change")
                        }

                        TextButton(
                            onClick = {
                                selectedLead = null
                                leadSearchQuery = ""
                                viewModel.searchLeads("")
                            },
                            enabled = !state.isSaving
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }


        CalendarCreateSectionCard(title = "Task") {
            if (selectedTask == null) {
                OutlinedTextField(
                    value = taskSearchQuery,
                    onValueChange = {
                        taskSearchQuery = it
                        viewModel.searchTasks(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task") },
                    placeholder = { Text("Search for a task") },
                    enabled = !state.isSaving,
                    singleLine = true
                )

                if (state.isLoadingTasks) {
                    CircularProgressIndicator()
                }

                state.taskSearchError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.taskSearchResults.forEach { task ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTask = task
                                taskSearchQuery = ""
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            task.statusName?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Status: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            task.leadName?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Lead: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            task.dueDate?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Due: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (!state.isLoadingTasks &&
                    taskSearchQuery.isNotBlank() &&
                    state.taskSearchResults.isEmpty() &&
                    state.taskSearchError == null
                ) {
                    Text("No tasks found.")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedTask?.title.orEmpty(),
                        style = MaterialTheme.typography.titleSmall
                    )

                    selectedTask?.statusName?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "Status: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    selectedTask?.leadName?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "Lead: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                selectedTask = null
                                taskSearchQuery = ""
                                viewModel.searchTasks("")
                            },
                            enabled = !state.isSaving
                        ) {
                            Text("Change")
                        }

                        TextButton(
                            onClick = {
                                selectedTask = null
                                taskSearchQuery = ""
                                viewModel.searchTasks("")
                            },
                            enabled = !state.isSaving
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        CalendarCreateSectionCard(title = "Schedule") {
            CalendarCreateDateTimeField(
                label = "Start",
                value = startDateTime,
                enabled = !state.isSaving,
                onChanged = {
                    startDateTime = it
                    validationError = null
                }
            )

            CalendarCreateDateTimeField(
                label = "End",
                value = endDateTime,
                enabled = !state.isSaving,
                onChanged = {
                    endDateTime = it
                    validationError = null
                }
            )

            FilterChip(
                selected = allDay,
                onClick = { allDay = !allDay },
                label = { Text("All Day") },
                enabled = !state.isSaving
            )
        }

        CalendarCreateSectionCard(title = "Location") {
            CalendarCreateTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                enabled = !state.isSaving
            )
        }

        CalendarCreateSectionCard(title = "Outcome") {
            CalendarCreateTextField(
                value = outcome,
                onValueChange = { outcome = it },
                label = "Outcome",
                enabled = !state.isSaving,
                singleLine = false,
                minLines = 2
            )
        }

        validationError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onBack,
                enabled = !state.isSaving
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val cleanTitle = title.trim()

                    validationError = when {
                        cleanTitle.isBlank() ->
                            "Title is required."

                        endDateTime.isBefore(startDateTime) ->
                            "End time cannot be before start time."

                        else -> null
                    }

                    if (validationError == null) {
                        val zone = ZoneId.systemDefault()

                        viewModel.save(
                            CalendarEventCreateRequest(
                                title = cleanTitle,
                                description = description.trim().ifBlank { null },
                                eventType = eventType,
                                startAt = startDateTime
                                    .atZone(zone)
                                    .toOffsetDateTime()
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                endAt = endDateTime
                                    .atZone(zone)
                                    .toOffsetDateTime()
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                allDay = allDay,
                                location = location.trim().ifBlank { null },
                                leadId = selectedLead?.id,
                                taskId = selectedTask?.id,
                                outcome = outcome.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Save Event")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CalendarCreateSectionCard(
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            content()
        }
    }
}

@Composable
private fun CalendarCreateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarCreateDateTimeField(
    label: String,
    value: LocalDateTime,
    enabled: Boolean,
    onChanged: (LocalDateTime) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy")
    }

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("hh:mm a")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = enabled) {
                        showDatePicker = true
                    }
            ) {
                OutlinedTextField(
                    value = value.toLocalDate().format(dateFormatter),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    label = { Text("Date") }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = enabled) {
                        showTimePicker = true
                    }
            ) {
                OutlinedTextField(
                    value = value.toLocalTime().format(timeFormatter),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    label = { Text("Time") }
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            onChanged(
                                LocalDateTime.of(
                                    selectedDate,
                                    value.toLocalTime()
                                )
                            )
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = value.hour,
            initialMinute = value.minute,
            is24Hour = false
        )

        DatePickerDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onChanged(
                            LocalDateTime.of(
                                value.toLocalDate(),
                                LocalTime.of(
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                            )
                        )

                        showTimePicker = false
                    }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(
                state = timePickerState
            )
        }
    }
}
