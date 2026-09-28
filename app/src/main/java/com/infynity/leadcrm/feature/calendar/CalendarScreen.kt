package com.infynity.leadcrm.feature.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import java.time.Instant
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onEventSelected: (Int) -> Unit = {},
    onNewEvent: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visibleEvents = viewModel.visibleEvents(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CalendarHeader(
            selectedDate = state.selectedDate,
            viewMode = state.viewMode,
            onPrevious = viewModel::previousPeriod,
            onToday = viewModel::today,
            onNext = viewModel::nextPeriod,
            onRefresh = viewModel::refresh,
            onNewEvent = onNewEvent
        )

        Spacer(modifier = Modifier.height(8.dp))

        CalendarViewSelector(
            selectedMode = state.viewMode,
            onModeSelected = viewModel::setViewMode
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search events") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CalendarFilters(
            selectedStatus = state.selectedStatus,
            selectedEventType = state.selectedEventType,
            onStatusSelected = viewModel::setStatus,
            onEventTypeSelected = viewModel::setEventType
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading && state.events.isEmpty() -> {
                CircularProgressIndicator()
            }

            state.errorMessage != null && state.events.isEmpty() -> {
                Text(
                    text = state.errorMessage ?: "Unable to load calendar",
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.viewMode == CalendarViewMode.MONTH -> {
                MonthCalendarGrid(
                    selectedDate = state.selectedDate,
                    events = visibleEvents,
                    onDateSelected = { date ->
                        viewModel.selectDateAndView(
                            date,
                            CalendarViewMode.DAY
                        )
                    },
                    onEventSelected = onEventSelected
                )
            }

            visibleEvents.isEmpty() -> {
                Text(
                    text = "No calendar events",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                when (state.viewMode) {
                    CalendarViewMode.AGENDA -> {
                        AgendaEventList(
                            events = visibleEvents,
                            onEventSelected = onEventSelected
                        )
                    }

                    CalendarViewMode.DAY -> {
                        DayEventList(
                            events = visibleEvents,
                            onEventSelected = onEventSelected
                        )
                    }

                    CalendarViewMode.WEEK -> {
                        WeekCalendarGrid(
                            selectedDate = state.selectedDate,
                            events = visibleEvents,
                            onDateSelected = { date ->
                                viewModel.selectDateAndView(
                                    date,
                                    CalendarViewMode.DAY
                                )
                            },
                            onEventSelected = onEventSelected
                        )
                    }

                    CalendarViewMode.MONTH -> {
                        MonthCalendarGrid(
                            selectedDate = state.selectedDate,
                            events = visibleEvents,
                            onDateSelected = { date ->
                                viewModel.selectDateAndView(
                                    date,
                                    CalendarViewMode.DAY
                                )
                            },
                            onEventSelected = onEventSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    selectedDate: LocalDate,
    viewMode: CalendarViewMode,
    onPrevious: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
    onRefresh: () -> Unit,
    onNewEvent: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous ${viewMode.name.lowercase()}"
            )
        }

        Column {
            Text(
                text = when (viewMode) {
                    CalendarViewMode.AGENDA -> {
                        "Agenda from ${selectedDate.format(formatter)}"
                    }

                    CalendarViewMode.DAY -> {
                        selectedDate.format(
                            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
                        )
                    }

                    CalendarViewMode.WEEK -> {
                        "Week of ${selectedDate
                            .with(java.time.DayOfWeek.MONDAY)
                            .format(formatter)}"
                    }

                    CalendarViewMode.MONTH -> {
                        selectedDate.format(
                            DateTimeFormatter.ofPattern("MMMM yyyy")
                        )
                    }
                },
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(
                onClick = onToday,
                modifier = Modifier.padding(0.dp)
            ) {
                Text("Today")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(onClick = onNewEvent) {
                Text("+ New Event")
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next ${viewMode.name.lowercase()}"
                )
            }

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
    }
}

@Composable
private fun CalendarViewSelector(
    selectedMode: CalendarViewMode,
    onModeSelected: (CalendarViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CalendarViewMode.entries.forEach { mode ->
            if (mode == selectedMode) {
                OutlinedButton(
                    onClick = { onModeSelected(mode) }
                ) {
                    Text(mode.displayName())
                }
            } else {
                TextButton(
                    onClick = { onModeSelected(mode) }
                ) {
                    Text(mode.displayName())
                }
            }
        }
    }
}

private fun CalendarViewMode.displayName(): String {
    return when (this) {
        CalendarViewMode.AGENDA -> "Agenda"
        CalendarViewMode.DAY -> "Day"
        CalendarViewMode.WEEK -> "Week"
        CalendarViewMode.MONTH -> "Month"
    }
}

@Composable
private fun AgendaEventList(
    events: List<CalendarEventResponse>,
    onEventSelected: (Int) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, dd MMMM")
    }

    val groupedEvents = events.groupBy { event ->
        parseEventInstant(event.startAt)
            ?.atZone(zoneId)
            ?.toLocalDate()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedEvents.forEach { (date, dayEvents) ->
            item {
                Text(
                    text = date?.format(dateFormatter) ?: "Date unavailable",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 2.dp
                    )
                )
            }

            items(
                items = dayEvents,
                key = { it.id }
            ) { event ->
                CalendarEventCard(
                    event = event,
                    onClick = {
                        onEventSelected(event.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DayEventList(
    events: List<CalendarEventResponse>,
    onEventSelected: (Int) -> Unit
) {
    EventList(
        events = events,
        onEventSelected = onEventSelected
    )
}

@Composable
private fun WeekEventList(
    events: List<CalendarEventResponse>,
    onEventSelected: (Int) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEE, dd MMM")
    }

    val groupedEvents = events.groupBy { event ->
        parseEventInstant(event.startAt)
            ?.atZone(zoneId)
            ?.toLocalDate()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedEvents.forEach { (date, dayEvents) ->
            item {
                Text(
                    text = date?.format(dateFormatter) ?: "Date unavailable",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(
                items = dayEvents,
                key = { it.id }
            ) { event ->
                CalendarEventCard(
                    event = event,
                    onClick = {
                        onEventSelected(event.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun WeekCalendarGrid(
    selectedDate: LocalDate,
    events: List<CalendarEventResponse>,
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (Int) -> Unit
) {
    val zoneId = ZoneId.systemDefault()

    val weekStart = selectedDate.with(
        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
    )

    val weekDates = (0L..6L).map {
        weekStart.plusDays(it)
    }

    val eventsByDate = events.groupBy { event ->
        parseEventInstant(event.startAt)
            ?.atZone(zoneId)
            ?.toLocalDate()
    }

    val hourHeight = 56.dp
    val timeColumnWidth = 56.dp
    val dayColumnWidth = 132.dp
    val headerHeight = 56.dp
    val firstHour = 9
    val lastHour = 18

    val horizontalScrollState = rememberScrollState()

    LaunchedEffect(selectedDate, weekStart) {
        val selectedIndex = weekDates.indexOf(selectedDate)

        if (selectedIndex >= 0) {
            val targetIndex = (selectedIndex - 1).coerceAtLeast(0)

            horizontalScrollState.animateScrollTo(
                targetIndex * 132
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header: fixed time-column spacer + horizontally scrollable dates.
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier.width(timeColumnWidth)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                weekDates.forEach { date ->
                    Column(
                        modifier = Modifier
                            .width(dayColumnWidth)
                            .height(headerHeight)
                            .clickable {
                                onDateSelected(date)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.format(
                                DateTimeFormatter.ofPattern("EEE")
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (date == LocalDate.now()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        // Body: fixed time column + horizontally scrollable event grid.
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.width(timeColumnWidth)
            ) {
                (firstHour until lastHour).forEach { hour ->
                    Box(
                        modifier = Modifier
                            .height(hourHeight)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                hour == 0 -> "12 AM"
                                hour < 12 -> "$hour AM"
                                hour == 12 -> "12 PM"
                                else -> "${hour - 12} PM"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(
                                top = 4.dp,
                                end = 4.dp
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(horizontalScrollState)
            ) {
                weekDates.forEach { date ->
                    val dayEvents = eventsByDate[date].orEmpty()

                    Box(
                        modifier = Modifier
                            .width(dayColumnWidth)
                            .height(
                                hourHeight * (lastHour - firstHour)
                            )
                    ) {
                        Column {
                            (firstHour until lastHour).forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(hourHeight)
                                ) {
                                    androidx.compose.material3.HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        dayEvents.forEach { event ->
                            val start = parseEventInstant(
                                event.startAt
                            )?.atZone(zoneId)

                            val end = parseEventInstant(
                                event.endAt
                            )?.atZone(zoneId)

                            if (start != null) {
                                val startMinutes =
                                    start.hour * 60 + start.minute

                                val endMinutes =
                                    if (end != null) {
                                        end.hour * 60 + end.minute
                                    } else {
                                        startMinutes + 30
                                    }

                                val topOffset =
                                    hourHeight *
                                        ((startMinutes / 60f) - firstHour)

                                val eventHeight =
                                    maxOf(
                                        40.dp,
                                        hourHeight *
                                            ((endMinutes - startMinutes) / 60f)
                                    )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 3.dp,
                                            end = 3.dp
                                        )
                                        .offset(y = topOffset)
                                        .height(eventHeight)
                                        .clickable {
                                            onEventSelected(event.id)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp)
                                    ) {
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 2
                                        )

                                        Text(
                                            text = event.status
                                                .formatFilterLabel(),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    selectedDate: LocalDate,
    events: List<CalendarEventResponse>,
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (Int) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val firstDay = selectedDate.withDayOfMonth(1)
    val gridStart = firstDay.with(
        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
    )
    val lastDay = selectedDate.withDayOfMonth(
        selectedDate.lengthOfMonth()
    )
    val gridEnd = lastDay.with(
        TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
    )

    val dates = generateSequence(gridStart) { date ->
        if (date.isBefore(gridEnd)) date.plusDays(1) else null
    }.toList()

    val eventsByDate = events.groupBy { event ->
        parseEventInstant(event.startAt)
            ?.atZone(zoneId)
            ?.toLocalDate()
    }

    val today = LocalDate.now()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                .forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = dates.chunked(7)
            ) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        val dayEvents = eventsByDate[date].orEmpty()
                        val isCurrentMonth =
                            date.month == selectedDate.month &&
                                date.year == selectedDate.year
                        val isToday = date == today
                        val isSelected = date == selectedDate

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clickable {
                                    onDateSelected(date)
                                },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when {
                                        isToday ->
                                            MaterialTheme.colorScheme.primary
                                        isSelected ->
                                            MaterialTheme.colorScheme.primary
                                        !isCurrentMonth ->
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else ->
                                            MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                if (dayEvents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (dayEvents.size == 1) {
                                            "1 event"
                                        } else {
                                            "${dayEvents.size} events"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthEventList(
    events: List<CalendarEventResponse>,
    onEventSelected: (Int) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEE, dd MMM")
    }

    val groupedEvents = events.groupBy { event ->
        parseEventInstant(event.startAt)
            ?.atZone(zoneId)
            ?.toLocalDate()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedEvents.forEach { (date, dayEvents) ->
            item {
                Text(
                    text = date?.format(dateFormatter) ?: "Date unavailable",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(
                items = dayEvents,
                key = { it.id }
            ) { event ->
                CalendarEventCard(
                    event = event,
                    onClick = {
                        onEventSelected(event.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun EventList(
    events: List<CalendarEventResponse>,
    onEventSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = events,
            key = { it.id }
        ) { event ->
            CalendarEventCard(
                event = event,
                onClick = {
                    onEventSelected(event.id)
                }
            )
        }
    }
}

@Composable
private fun CalendarFilters(
    selectedStatus: String?,
    selectedEventType: String?,
    onStatusSelected: (String?) -> Unit,
    onEventTypeSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterMenu(
            label = selectedStatus?.formatFilterLabel() ?: "Status",
            options = listOf(
                "scheduled",
                "completed",
                "cancelled",
                "no_show"
            ),
            selected = selectedStatus,
            onSelected = onStatusSelected,
            modifier = Modifier.weight(1f)
        )

        FilterMenu(
            label = selectedEventType?.formatFilterLabel() ?: "Type",
            options = listOf(
                "general",
                "meeting",
                "call",
                "follow_up",
                "site_visit",
                "installation",
                "payment",
                "other"
            ),
            selected = selectedEventType,
            onSelected = onEventTypeSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FilterMenu(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    expanded = false
                    onSelected(null)
                }
            )

            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.formatFilterLabel()) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    }
                )
            }
        }
    }
}

private fun String.formatFilterLabel(): String {
    return replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}

@Composable
private fun CalendarEventCard(
    event: CalendarEventResponse,
    onClick: () -> Unit
) {
    val start = parseEventInstant(event.startAt)
    val end = parseEventInstant(event.endAt)

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a")
    }

    val timeText = when {
        start != null && end != null -> {
            "${start.atZone(ZoneId.systemDefault()).format(timeFormatter)} – " +
                end.atZone(ZoneId.systemDefault()).format(timeFormatter)
        }

        start != null -> {
            start.atZone(ZoneId.systemDefault()).format(timeFormatter)
        }

        else -> {
            "Time unavailable"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${event.eventType.formatFilterLabel()} • " +
                    event.status.formatFilterLabel(),
                style = MaterialTheme.typography.bodySmall
            )

            event.leadName?.let {
                Text(
                    text = "Lead: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            event.taskTitle?.let {
                Text(
                    text = "Task: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            event.location?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Location: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun parseEventInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null

    return try {
        Instant.parse(value.trim())
    } catch (_: Exception) {
        try {
            java.time.OffsetDateTime.parse(value.trim()).toInstant()
        } catch (_: Exception) {
            null
        }
    }
}
