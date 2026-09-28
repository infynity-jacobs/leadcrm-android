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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            initializedEventId = event.id
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
                    linkedLead = uiState.event?.leadName,
                    linkedTask = uiState.event?.taskTitle,
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
    linkedLead: String?,
    linkedTask: String?,
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

        if (linkedLead != null ||
            linkedTask != null ||
            assignedTo != null ||
            teamName != null
        ) {
            CalendarEditSectionCard(title = "Links & Assignment") {
                linkedLead?.let {
                    CalendarReadOnlyValue(
                        label = "Lead",
                        value = it
                    )
                }

                linkedTask?.let {
                    CalendarReadOnlyValue(
                        label = "Task",
                        value = it
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

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
