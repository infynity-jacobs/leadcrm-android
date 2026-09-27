package com.infynity.leadcrm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val tasks: List<TaskResponse>,
        val calendarEvents: List<CalendarEventResponse>,
        val leads: List<LeadResponse>
    ) : HomeUiState

    data class Error(
        val message: String
    ) : HomeUiState
}

class HomeViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                val data = repository.loadDashboard()
                val today = LocalDate.now()

                val todayTasks = data.tasks
                    .filter { task ->
                        task.dueDate?.let { dueDate ->
                            parseDate(dueDate) == today
                        } == true
                    }
                    .filterNot { it.statusIsClosed }
                    .sortedBy { it.dueDate }

                val upcomingEvents = data.calendarEvents
                    .filter { it.status.lowercase() != "cancelled" }
                    .sortedBy { it.startAt }

                _uiState.value = HomeUiState.Success(
                    tasks = todayTasks,
                    calendarEvents = upcomingEvents,
                    leads = data.leads
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    e.message ?: "Unable to load dashboard"
                )
            }
        }
    }

    private fun parseDate(value: String): LocalDate? {
        return try {
            Instant.parse(value)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } catch (_: Exception) {
            null
        }
    }
}
