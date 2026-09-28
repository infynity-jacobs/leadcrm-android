package com.infynity.leadcrm.feature.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CalendarEventDetailScreen(
    viewModel: CalendarEventDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Calendar Event",
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
                val event = uiState.event!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CalendarEventDetailCard(
                        title = "Event",
                        content = {
                            DetailValue(
                                label = "Title",
                                value = event.title
                            )

                            DetailValue(
                                label = "Status",
                                value = formatCalendarValue(event.status)
                            )

                            DetailValue(
                                label = "Event Type",
                                value = formatCalendarValue(event.eventType)
                            )

                            DetailValue(
                                label = "Schedule",
                                value = formatEventSchedule(
                                    event.startAt,
                                    event.endAt,
                                    event.allDay
                                )
                            )
                        }
                    )

                    if (!event.location.isNullOrBlank()) {
                        CalendarEventDetailCard(
                            title = "Location",
                            content = {
                                DetailValue(
                                    label = "Location",
                                    value = event.location
                                )
                            }
                        )
                    }

                    if (event.leadName != null ||
                        event.taskTitle != null ||
                        event.assignedToName != null ||
                        event.teamName != null
                    ) {
                        CalendarEventDetailCard(
                            title = "Links & Assignment",
                            content = {
                                event.leadName?.let {
                                    DetailValue(
                                        label = "Lead",
                                        value = it
                                    )
                                }

                                event.taskTitle?.let {
                                    DetailValue(
                                        label = "Task",
                                        value = it
                                    )
                                }

                                event.assignedToName?.let {
                                    DetailValue(
                                        label = "Assigned To",
                                        value = it
                                    )
                                }

                                event.teamName?.let {
                                    DetailValue(
                                        label = "Team",
                                        value = it
                                    )
                                }
                            }
                        )
                    }

                    if (!event.description.isNullOrBlank() ||
                        !event.outcome.isNullOrBlank()
                    ) {
                        CalendarEventDetailCard(
                            title = "Details",
                            content = {
                                event.description?.let {
                                    if (it.isNotBlank()) {
                                        DetailValue(
                                            label = "Description",
                                            value = it
                                        )
                                    }
                                }

                                event.outcome?.let {
                                    if (it.isNotBlank()) {
                                        DetailValue(
                                            label = "Outcome",
                                            value = it
                                        )
                                    }
                                }
                            }
                        )
                    }

                    if (event.completedByName != null ||
                        event.completedAt != null
                    ) {
                        CalendarEventDetailCard(
                            title = "Completion",
                            content = {
                                event.completedByName?.let {
                                    DetailValue(
                                        label = "Completed By",
                                        value = it
                                    )
                                }

                                event.completedAt?.let {
                                    DetailValue(
                                        label = "Completed At",
                                        value = formatEventDateTime(it)
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CalendarEventDetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
private fun DetailValue(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
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

private fun formatEventSchedule(
    startAt: String,
    endAt: String,
    allDay: Boolean
): String {
    if (allDay) {
        return formatEventDate(startAt)
    }

    val start = parseEventDateTime(startAt)
    val end = parseEventDateTime(endAt)

    if (start == null || end == null) {
        return "$startAt – $endAt"
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    val startDate = start.toLocalDate()
    val endDate = end.toLocalDate()

    return if (startDate == endDate) {
        "${startDate.format(dateFormatter)}, " +
            "${start.toLocalTime().format(timeFormatter)} – " +
            end.toLocalTime().format(timeFormatter)
    } else {
        "${start.format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))} – " +
            end.format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))
    }
}

private fun formatEventDateTime(value: String): String {
    val parsed = parseEventDateTime(value)
        ?: return value

    return parsed.format(
        DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a")
    )
}

private fun formatEventDate(value: String): String {
    val parsed = parseEventDateTime(value)
        ?: return value

    return parsed.format(
        DateTimeFormatter.ofPattern("dd MMM yyyy")
    )
}

private fun parseEventDateTime(value: String): OffsetDateTime? {
    return try {
        OffsetDateTime.parse(value)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toOffsetDateTime()
    } catch (_: Exception) {
        null
    }
}
