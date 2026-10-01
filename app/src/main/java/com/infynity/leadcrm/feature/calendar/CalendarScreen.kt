package com.infynity.leadcrm.feature.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Surface
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

        CalendarFilterControl(
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
            OutlinedButton(
                onClick = onNewEvent,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 0.dp
                ),
                modifier = Modifier.height(36.dp)
            ) {
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
                    onClick = { onModeSelected(mode) },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 0.dp
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(mode.displayName())
                }
            } else {
                TextButton(
                    onClick = { onModeSelected(mode) },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 10.dp,
                        vertical = 0.dp
                    ),
                    modifier = Modifier.height(36.dp)
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

                        val placements = buildWeekEventPlacements(
                            events = dayEvents,
                            zoneId = zoneId
                        )

                        placements.forEach { placement ->
                            val topOffset =
                                hourHeight *
                                    ((placement.startMinutes / 60f) - firstHour)

                            val eventHeight =
                                maxOf(
                                    40.dp,
                                    hourHeight *
                                        ((placement.endMinutes - placement.startMinutes) / 60f)
                                )

                            val columnWidth =
                                dayColumnWidth / placement.columnCount

                            Surface(
                                modifier = Modifier
                                    .width(columnWidth)
                                    .padding(horizontal = 2.dp)
                                    .offset(
                                        x = columnWidth * placement.column,
                                        y = topOffset
                                    )
                                    .height(eventHeight)
                                    .clickable {
                                        onEventSelected(placement.event.id)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                val compact = placement.columnCount >= 3

                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = if (compact) 4.dp else 5.dp,
                                        vertical = if (compact) 4.dp else 5.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(
                                        if (compact) 1.dp else 2.dp
                                    )
                                ) {
                                    Text(
                                        text = placement.event.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                        maxLines = if (compact) 3 else 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (compact) {
                                        val statusColor = when (
                                            placement.event.status.lowercase()
                                        ) {
                                            "completed" ->
                                                MaterialTheme.colorScheme.primary
                                            "cancelled", "no_show" ->
                                                MaterialTheme.colorScheme.error
                                            else ->
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height(6.dp),
                                            shape = RoundedCornerShape(50),
                                            color = statusColor
                                        ) {}
                                    } else {
                                        Text(
                                            text = placement.event.status
                                                .formatFilterLabel(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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

private data class WeekEventPlacement(
    val event: CalendarEventResponse,
    val startMinutes: Int,
    val endMinutes: Int,
    val column: Int,
    val columnCount: Int
)

private data class ParsedWeekEvent(
    val event: CalendarEventResponse,
    val startMinutes: Int,
    val endMinutes: Int
)

private fun buildWeekEventPlacements(
    events: List<CalendarEventResponse>,
    zoneId: ZoneId
): List<WeekEventPlacement> {
    val parsed = events.mapNotNull { event ->
        val start = parseEventInstant(event.startAt)
            ?.atZone(zoneId)

        if (start == null) {
            null
        } else {
            val startMinutes = start.hour * 60 + start.minute

            val parsedEnd = parseEventInstant(event.endAt)
                ?.atZone(zoneId)

            val endMinutes = if (parsedEnd != null) {
                parsedEnd.hour * 60 + parsedEnd.minute
            } else {
                startMinutes + 30
            }

            ParsedWeekEvent(
                event = event,
                startMinutes = startMinutes,
                endMinutes = maxOf(
                    startMinutes + 30,
                    endMinutes
                )
            )
        }
    }.sortedWith(
        compareBy<ParsedWeekEvent> { it.startMinutes }
            .thenBy { it.endMinutes }
    )

    if (parsed.isEmpty()) {
        return emptyList()
    }

    val placements = mutableListOf<WeekEventPlacement>()
    var clusterStart = 0

    while (clusterStart < parsed.size) {
        var clusterEnd = clusterStart
        var clusterEndTime = parsed[clusterStart].endMinutes

        while (
            clusterEnd + 1 < parsed.size &&
            parsed[clusterEnd + 1].startMinutes < clusterEndTime
        ) {
            clusterEnd++
            clusterEndTime = maxOf(
                clusterEndTime,
                parsed[clusterEnd].endMinutes
            )
        }

        val cluster = parsed.subList(
            clusterStart,
            clusterEnd + 1
        )

        val columnEnds = mutableListOf<Int>()
        val assignedColumns = mutableListOf<Int>()

        cluster.forEach { event ->
            var column = columnEnds.indexOfFirst {
                it <= event.startMinutes
            }

            if (column == -1) {
                column = columnEnds.size
                columnEnds.add(event.endMinutes)
            } else {
                columnEnds[column] = event.endMinutes
            }

            assignedColumns.add(column)
        }

        val columnCount = columnEnds.size

        cluster.forEachIndexed { index, event ->
            placements.add(
                WeekEventPlacement(
                    event = event.event,
                    startMinutes = event.startMinutes,
                    endMinutes = event.endMinutes,
                    column = assignedColumns[index],
                    columnCount = columnCount
                )
            )
        }

        clusterStart = clusterEnd + 1
    }

    return placements
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

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp)
                                .clickable {
                                    onDateSelected(date)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isToday) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = 6.dp,
                                        vertical = 7.dp
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isToday || isSelected) {
                                        androidx.compose.ui.text.font.FontWeight.SemiBold
                                    } else {
                                        androidx.compose.ui.text.font.FontWeight.Normal
                                    },
                                    color = when {
                                        isToday || isSelected ->
                                            MaterialTheme.colorScheme.primary
                                        !isCurrentMonth ->
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else ->
                                            MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                if (dayEvents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(5.dp))

                                    Text(
                                        text = if (dayEvents.size == 1) {
                                            "1 event"
                                        } else {
                                            "${dayEvents.size} events"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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
private fun CalendarFilterControl(
    selectedStatus: String?,
    selectedEventType: String?,
    onStatusSelected: (String?) -> Unit,
    onEventTypeSelected: (String?) -> Unit
) {
    var filterDialogOpen by remember { mutableStateOf(false) }

    val activeFilterCount =
        listOf(
            selectedStatus != null,
            selectedEventType != null
        ).count { it }

    OutlinedButton(
        onClick = { filterDialogOpen = true },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 14.dp,
            vertical = 0.dp
        ),
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = if (activeFilterCount == 0) {
                "Filters"
            } else {
                "Filters · $activeFilterCount"
            }
        )
    }

    if (filterDialogOpen) {
        CalendarFiltersDialog(
            selectedStatus = selectedStatus,
            selectedEventType = selectedEventType,
            onDismiss = { filterDialogOpen = false },
            onApply = { status, eventType ->
                onStatusSelected(status)
                onEventTypeSelected(eventType)
                filterDialogOpen = false
            },
            onReset = {
                onStatusSelected(null)
                onEventTypeSelected(null)
                filterDialogOpen = false
            }
        )
    }
}

@Composable
private fun CalendarFiltersDialog(
    selectedStatus: String?,
    selectedEventType: String?,
    onDismiss: () -> Unit,
    onApply: (String?, String?) -> Unit,
    onReset: () -> Unit
) {
    var localStatus by remember { mutableStateOf(selectedStatus) }
    var localEventType by remember { mutableStateOf(selectedEventType) }

    val statusOptions = listOf(
        "scheduled",
        "completed",
        "cancelled",
        "no_show"
    )

    val eventTypeOptions = listOf(
        "general",
        "meeting",
        "call",
        "follow_up",
        "site_visit",
        "installation",
        "payment",
        "other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CalendarFilterSection("Status") {
                    FilterChip(
                        selected = localStatus == null,
                        onClick = { localStatus = null },
                        label = { Text("All") }
                    )

                    statusOptions.forEach { option ->
                        FilterChip(
                            selected = localStatus == option,
                            onClick = { localStatus = option },
                            label = { Text(option.formatFilterLabel()) }
                        )
                    }
                }

                CalendarFilterSection("Type") {
                    FilterChip(
                        selected = localEventType == null,
                        onClick = { localEventType = null },
                        label = { Text("All") }
                    )

                    eventTypeOptions.forEach { option ->
                        FilterChip(
                            selected = localEventType == option,
                            onClick = { localEventType = option },
                            label = { Text(option.formatFilterLabel()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onApply(localStatus, localEventType)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onReset) {
                    Text("Reset")
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun CalendarFilterSection(
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
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

    val statusLabel = event.status.formatFilterLabel()
    val typeLabel = event.eventType.formatFilterLabel()

    val statusContainer = when (event.status.lowercase()) {
        "completed" -> MaterialTheme.colorScheme.primaryContainer
        "cancelled", "no_show" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val statusContent = when (event.status.lowercase()) {
        "completed" -> MaterialTheme.colorScheme.onPrimaryContainer
        "cancelled", "no_show" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusContainer
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusContent,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }

                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            event.leadName?.let {
                Text(
                    text = "Lead: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            event.taskTitle?.let {
                Text(
                    text = "Task: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            event.location?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Location: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
