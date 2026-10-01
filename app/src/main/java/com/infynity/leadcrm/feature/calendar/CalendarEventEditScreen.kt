package com.infynity.leadcrm.feature.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.CalendarEventUpdateRequest
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TextButton

@Composable
fun CalendarEventEditScreen(
    viewModel: CalendarEventEditViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var initializedEventId by remember { mutableStateOf<Int?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("general") }
    var startDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var endDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var allDay by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("scheduled") }
    var outcome by remember { mutableStateOf("") }

    var selectedLead by remember { mutableStateOf<LeadResponse?>(null) }
    var leadSearchQuery by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<TaskResponse?>(null) }
    var taskSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uiState.event?.id) {
        val event = uiState.event ?: return@LaunchedEffect

        if (initializedEventId != event.id) {
            title = event.title
            description = event.description.orEmpty()
            eventType = event.eventType
            startDateTime = parseEventLocalDateTime(event.startAt)
            endDateTime = parseEventLocalDateTime(event.endAt)
            allDay = event.allDay
            location = event.location.orEmpty()
            status = event.status
            outcome = event.outcome.orEmpty()

            selectedLead = null
            selectedTask = null
            leadSearchQuery = ""
            taskSearchQuery = ""

            event.leadId?.let { leadId ->
                viewModel.loadLeadForSelection(
                    leadId = leadId,
                    leadName = event.leadName
                )
            }

            event.taskId?.let { taskId ->
                selectedTask = uiState.allTasks.firstOrNull {
                    it.id == taskId
                }
            }

            initializedEventId = event.id
        }
    }

    LaunchedEffect(uiState.selectedLead?.id) {
        uiState.selectedLead?.let {
            selectedLead = it
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
                text = "Edit Calendar Event",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }

        when {
            uiState.isLoading && uiState.event == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.event == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage
                            ?: "Unable to load calendar event",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = viewModel::loadEvent) {
                        Text("Retry")
                    }
                }
            }

            uiState.event != null -> {
                CalendarEventEditForm(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    eventType = eventType,
                    onEventTypeChange = { eventType = it },
                    startDateTime = startDateTime,
                    onStartDateTimeChange = { startDateTime = it },
                    endDateTime = endDateTime,
                    onEndDateTimeChange = { endDateTime = it },
                    allDay = allDay,
                    onAllDayChange = { allDay = it },
                    location = location,
                    onLocationChange = { location = it },
                    status = status,
                    onStatusChange = { status = it },
                    outcome = outcome,
                    onOutcomeChange = { outcome = it },
                    selectedLead = selectedLead,
                    leadSearchQuery = leadSearchQuery,
                    onLeadSearchQueryChange = {
                        leadSearchQuery = it
                        viewModel.searchLeads(it)
                    },
                    onLeadSelected = {
                        selectedLead = it
                        leadSearchQuery = ""
                    },
                    onLeadClear = {
                        selectedLead = null
                        leadSearchQuery = ""
                        viewModel.searchLeads("")
                    },
                    selectedTask = selectedTask,
                    taskSearchQuery = taskSearchQuery,
                    onTaskSearchQueryChange = {
                        taskSearchQuery = it
                        viewModel.searchTasks(it)
                    },
                    onTaskSelected = {
                        selectedTask = it
                        taskSearchQuery = ""
                    },
                    onTaskClear = {
                        selectedTask = null
                        taskSearchQuery = ""
                        viewModel.searchTasks("")
                    },
                    leadSearchResults = uiState.leadSearchResults,
                    isSearchingLeads = uiState.isSearchingLeads,
                    leadSearchError = uiState.leadSearchError,
                    taskSearchResults = uiState.taskSearchResults,
                    isLoadingTasks = uiState.isLoadingTasks,
                    taskSearchError = uiState.taskSearchError,
                    assignedTo = uiState.event?.assignedToName,
                    teamName = uiState.event?.teamName,
                    isSaving = uiState.isSaving,
                    errorMessage = uiState.errorMessage,
                    onCancel = onBack,
                    onSave = {
                        val start = startDateTime
                        val end = endDateTime

                        if (start != null &&
                            end != null &&
                            !end.isBefore(start)
                        ) {
                            viewModel.save(
                                CalendarEventUpdateRequest(
                                    title = title.trim(),
                                    description = description
                                        .trim()
                                        .ifBlank { null },
                                    eventType = eventType.ifBlank {
                                        null
                                    },
                                    startAt = start
                                        .atZone(ZoneId.systemDefault())
                                        .format(
                                            DateTimeFormatter
                                                .ISO_OFFSET_DATE_TIME
                                        ),
                                    endAt = end
                                        .atZone(ZoneId.systemDefault())
                                        .format(
                                            DateTimeFormatter
                                                .ISO_OFFSET_DATE_TIME
                                        ),
                                    allDay = allDay,
                                    location = location
                                        .trim()
                                        .ifBlank { null },
                                    leadId = selectedLead?.id,
                                    taskId = selectedTask?.id,
                                    status = status.ifBlank { null },
                                    outcome = outcome
                                        .trim()
                                        .ifBlank { null }
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
private fun CalendarEventEditForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    eventType: String,
    onEventTypeChange: (String) -> Unit,
    startDateTime: LocalDateTime?,
    onStartDateTimeChange: (LocalDateTime) -> Unit,
    endDateTime: LocalDateTime?,
    onEndDateTimeChange: (LocalDateTime) -> Unit,
    allDay: Boolean,
    onAllDayChange: (Boolean) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit,
    outcome: String,
    onOutcomeChange: (String) -> Unit,
    selectedLead: LeadResponse?,
    leadSearchQuery: String,
    onLeadSearchQueryChange: (String) -> Unit,
    onLeadSelected: (LeadResponse) -> Unit,
    onLeadClear: () -> Unit,
    selectedTask: TaskResponse?,
    taskSearchQuery: String,
    onTaskSearchQueryChange: (String) -> Unit,
    onTaskSelected: (TaskResponse) -> Unit,
    onTaskClear: () -> Unit,
    leadSearchResults: List<LeadResponse>,
    isSearchingLeads: Boolean,
    leadSearchError: String?,
    taskSearchResults: List<TaskResponse>,
    isLoadingTasks: Boolean,
    taskSearchError: String?,
    assignedTo: String?,
    teamName: String?,
    isSaving: Boolean,
    errorMessage: String?,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CalendarEditSectionCard(title = "Event") {
            CalendarEditTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "Title",
                enabled = !isSaving,
                required = true
            )

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

        CalendarEditSectionCard(title = "Event Type") {
            CalendarFilterRow {
                listOf(
                    "general",
                    "meeting",
                    "call",
                    "follow_up",
                    "site_visit",
                    "installation",
                    "payment"
                ).forEach { value ->
                    FilterChip(
                        selected = eventType.equals(
                            value,
                            ignoreCase = true
                        ),
                        onClick = {
                            if (!isSaving) {
                                onEventTypeChange(value)
                            }
                        },
                        label = {
                            Text(formatCalendarValue(value))
                        },
                        enabled = !isSaving
                    )
                }
            }
        }

        CalendarEditSectionCard(title = "Status") {
            CalendarFilterRow {
                listOf(
                    "scheduled",
                    "completed",
                    "cancelled",
                    "no_show"
                ).forEach { value ->
                    FilterChip(
                        selected = status.equals(
                            value,
                            ignoreCase = true
                        ),
                        onClick = {
                            if (!isSaving) {
                                onStatusChange(value)
                            }
                        },
                        label = {
                            Text(formatCalendarValue(value))
                        },
                        enabled = !isSaving
                    )
                }
            }
        }

        CalendarEditSectionCard(title = "Schedule") {
            CalendarDateTimeField(
                label = "Start Date & Time",
                value = startDateTime,
                enabled = !isSaving,
                onValueChange = onStartDateTimeChange
            )

            CalendarDateTimeField(
                label = "End Date & Time",
                value = endDateTime,
                enabled = !isSaving,
                onValueChange = onEndDateTimeChange
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving) {
                        onAllDayChange(!allDay)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = allDay,
                    onClick = {
                        if (!isSaving) {
                            onAllDayChange(!allDay)
                        }
                    },
                    label = { Text("All Day") },
                    enabled = !isSaving
                )
            }

            if (startDateTime != null &&
                endDateTime != null &&
                endDateTime.isBefore(startDateTime)
            ) {
                Text(
                    text = "End date/time cannot be earlier than the start date/time.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        CalendarEditSectionCard(title = "Location") {
            CalendarEditTextField(
                value = location,
                onValueChange = onLocationChange,
                label = "Location",
                enabled = !isSaving
            )
        }

        CalendarEditSectionCard(title = "Outcome") {
            OutlinedTextField(
                value = outcome,
                onValueChange = onOutcomeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                label = { Text("Outcome") },
                minLines = 3,
                enabled = !isSaving
            )
        }

        CalendarEditSectionCard(title = "Links & Assignment") {
            Text(
                text = "Lead",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            if (selectedLead == null) {
                OutlinedTextField(
                    value = leadSearchQuery,
                    onValueChange = onLeadSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search Lead") },
                    placeholder = { Text("Search by name, phone or company") },
                    singleLine = true,
                    enabled = !isSaving
                )

                if (isSearchingLeads) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterHorizontally),
                        strokeWidth = 2.dp
                    )
                }

                leadSearchResults.forEach { lead ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSaving) {
                                onLeadSelected(lead)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = listOfNotNull(
                                    lead.firstName,
                                    lead.lastName
                                ).joinToString(" ")
                                    .ifBlank { "Unnamed Lead" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            lead.company?.takeIf {
                                it.isNotBlank()
                            }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            lead.phone?.takeIf {
                                it.isNotBlank()
                            }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                leadSearchError?.takeIf {
                    it.isNotBlank()
                }?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = listOfNotNull(
                                selectedLead.firstName,
                                selectedLead.lastName
                            ).joinToString(" ")
                                .ifBlank { "Unnamed Lead" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        selectedLead.company?.takeIf {
                            it.isNotBlank()
                        }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onLeadClear,
                                enabled = !isSaving
                            ) {
                                Text("Change")
                            }

                            TextButton(
                                onClick = onLeadClear,
                                enabled = !isSaving
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }

            Text(
                text = "Task",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            if (selectedTask == null) {
                OutlinedTextField(
                    value = taskSearchQuery,
                    onValueChange = onTaskSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search Task") },
                    placeholder = { Text("Search by task title or lead") },
                    singleLine = true,
                    enabled = !isSaving
                )

                if (isLoadingTasks) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterHorizontally),
                        strokeWidth = 2.dp
                    )
                }

                taskSearchResults.forEach { task ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSaving) {
                                onTaskSelected(task)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            task.leadName?.takeIf {
                                it.isNotBlank()
                            }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            task.statusName?.takeIf {
                                it.isNotBlank()
                            }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                taskSearchError?.takeIf {
                    it.isNotBlank()
                }?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = selectedTask.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        selectedTask.statusName?.takeIf {
                            it.isNotBlank()
                        }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        selectedTask.leadName?.takeIf {
                            it.isNotBlank()
                        }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onTaskClear,
                                enabled = !isSaving
                            ) {
                                Text("Change")
                            }

                            TextButton(
                                onClick = onTaskClear,
                                enabled = !isSaving
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }

            assignedTo?.let {
                CalendarReadOnlyValue(
                    label = "Assigned To",
                    value = it
                )
            }

            teamName?.let {
                CalendarReadOnlyValue(
                    label = "Team",
                    value = it
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
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
                    startDateTime != null &&
                    endDateTime != null &&
                    !endDateTime.isBefore(startDateTime)
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

@Composable
private fun CalendarFilterRow(
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
private fun CalendarDateTimeField(
    label: String,
    value: LocalDateTime?,
    enabled: Boolean,
    onValueChange: (LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val displayFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a")

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
private fun CalendarEditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    required: Boolean = false
) {
    val keyboardController =
        LocalSoftwareKeyboardController.current

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
private fun CalendarEditSectionCard(
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
private fun CalendarReadOnlyValue(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun formatCalendarValue(value: String): String {
    return value
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}

private fun parseEventLocalDateTime(value: String): LocalDateTime? {
    return try {
        OffsetDateTime.parse(value)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(value)
        } catch (_: Exception) {
            null
        }
    }
}
