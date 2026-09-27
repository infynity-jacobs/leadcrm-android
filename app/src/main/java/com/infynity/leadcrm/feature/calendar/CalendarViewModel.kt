package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

enum class CalendarViewMode {
    AGENDA,
    DAY,
    WEEK,
    MONTH
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val events: List<CalendarEventResponse> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val viewMode: CalendarViewMode = CalendarViewMode.AGENDA,
    val searchQuery: String = "",
    val selectedStatus: String? = null,
    val selectedEventType: String? = null,
    val selectedEvent: CalendarEventResponse? = null,
    val errorMessage: String? = null,
    val operationError: String? = null
)

class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    init {
        loadEvents()
    }

    fun loadEvents() {
        val state = _uiState.value
        val (startDate, endDate) = visibleDateRange(
            state.selectedDate,
            state.viewMode
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val startAt = startDate
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toString()

                val endAt = endDate
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toString()

                val response = repository.getEvents(
                    startAt = startAt,
                    endAt = endAt
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = response.items,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load calendar events"
                )
            }
        }
    }

    fun setViewMode(mode: CalendarViewMode) {
        _uiState.value = _uiState.value.copy(
            viewMode = mode,
            selectedEvent = null
        )
        loadEvents()
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedEvent = null
        )
        loadEvents()
    }

    fun selectDateAndView(date: LocalDate, mode: CalendarViewMode) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            viewMode = mode,
            selectedEvent = null
        )
        loadEvents()
    }

    fun previousPeriod() {
        val state = _uiState.value

        val newDate = when (state.viewMode) {
            CalendarViewMode.AGENDA -> state.selectedDate.minusDays(30)
            CalendarViewMode.DAY -> state.selectedDate.minusDays(1)
            CalendarViewMode.WEEK -> state.selectedDate.minusWeeks(1)
            CalendarViewMode.MONTH -> state.selectedDate.minusMonths(1)
        }

        selectDate(newDate)
    }

    fun nextPeriod() {
        val state = _uiState.value

        val newDate = when (state.viewMode) {
            CalendarViewMode.AGENDA -> state.selectedDate.plusDays(30)
            CalendarViewMode.DAY -> state.selectedDate.plusDays(1)
            CalendarViewMode.WEEK -> state.selectedDate.plusWeeks(1)
            CalendarViewMode.MONTH -> state.selectedDate.plusMonths(1)
        }

        selectDate(newDate)
    }

    fun today() {
        selectDate(LocalDate.now())
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    fun setStatus(status: String?) {
        _uiState.value = _uiState.value.copy(
            selectedStatus = status
        )
    }

    fun setEventType(eventType: String?) {
        _uiState.value = _uiState.value.copy(
            selectedEventType = eventType
        )
    }

    fun selectEvent(event: CalendarEventResponse?) {
        _uiState.value = _uiState.value.copy(
            selectedEvent = event,
            operationError = null
        )
    }

    fun clearSelectedEvent() {
        _uiState.value = _uiState.value.copy(
            selectedEvent = null,
            operationError = null
        )
    }

    fun visibleEvents(state: CalendarUiState): List<CalendarEventResponse> {
        val query = state.searchQuery.trim()

        return state.events
            .asSequence()
            .filter { event ->
                state.selectedStatus == null ||
                    event.status.equals(state.selectedStatus, ignoreCase = true)
            }
            .filter { event ->
                state.selectedEventType == null ||
                    event.eventType.equals(state.selectedEventType, ignoreCase = true)
            }
            .filter { event ->
                query.isBlank() ||
                    event.title.contains(query, ignoreCase = true) ||
                    event.description.orEmpty().contains(query, ignoreCase = true) ||
                    event.location.orEmpty().contains(query, ignoreCase = true) ||
                    event.leadName.orEmpty().contains(query, ignoreCase = true) ||
                    event.taskTitle.orEmpty().contains(query, ignoreCase = true) ||
                    event.assignedToName.orEmpty().contains(query, ignoreCase = true)
            }
            .sortedBy { it.startAt }
            .toList()
    }

    fun refresh() {
        loadEvents()
    }

    private fun visibleDateRange(
        selectedDate: LocalDate,
        viewMode: CalendarViewMode
    ): Pair<LocalDate, LocalDate> {
        return when (viewMode) {
            CalendarViewMode.AGENDA -> {
                selectedDate to selectedDate.plusDays(30)
            }

            CalendarViewMode.DAY -> {
                selectedDate to selectedDate.plusDays(1)
            }

            CalendarViewMode.WEEK -> {
                val start = selectedDate.with(
                    java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                )
                start to start.plusWeeks(1)
            }

            CalendarViewMode.MONTH -> {
                val start = selectedDate.withDayOfMonth(1)
                start to start.plusMonths(1)
            }
        }
    }
}
