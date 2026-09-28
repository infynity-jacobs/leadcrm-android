package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventCreateRequest
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.data.repository.CalendarRepository
import com.infynity.leadcrm.data.repository.LeadRepository
import com.infynity.leadcrm.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarEventCreateUiState(
    val isSaving: Boolean = false,
    val createdEvent: CalendarEventResponse? = null,
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false,
    val isSearchingLeads: Boolean = false,
    val leadSearchResults: List<com.infynity.leadcrm.core.network.models.LeadResponse> = emptyList(),
    val leadSearchError: String? = null,
    val isLoadingTasks: Boolean = false,
    val allTasks: List<com.infynity.leadcrm.core.network.models.TaskResponse> = emptyList(),
    val taskSearchResults: List<com.infynity.leadcrm.core.network.models.TaskResponse> = emptyList(),
    val taskSearchError: String? = null
)

class CalendarEventCreateViewModel(
    private val repository: CalendarRepository,
    private val leadRepository: LeadRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarEventCreateUiState())

    init {
        loadTasks()
    }

    val uiState: StateFlow<CalendarEventCreateUiState> = _uiState.asStateFlow()

    fun searchLeads(query: String) {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isSearchingLeads = false,
                leadSearchResults = emptyList(),
                leadSearchError = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingLeads = true,
                leadSearchResults = emptyList(),
                leadSearchError = null
            )

            try {
                val response = leadRepository.getLeads(
                    page = 1,
                    pageSize = 25,
                    search = cleanQuery
                )

                _uiState.value = _uiState.value.copy(
                    isSearchingLeads = false,
                    leadSearchResults = response.items,
                    leadSearchError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearchingLeads = false,
                    leadSearchResults = emptyList(),
                    leadSearchError = e.message ?: "Unable to search leads"
                )
            }
        }
    }


    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingTasks = true,
                taskSearchError = null
            )

            try {
                val response = taskRepository.getTasks()
                _uiState.value = _uiState.value.copy(
                    isLoadingTasks = false,
                    allTasks = response.items,
                    taskSearchResults = emptyList(),
                    taskSearchError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingTasks = false,
                    taskSearchError = e.message ?: "Unable to load tasks"
                )
            }
        }
    }

    fun searchTasks(query: String) {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                taskSearchResults = emptyList(),
                taskSearchError = null
            )
            return
        }

        val normalizedQuery = cleanQuery.lowercase()

        val results = _uiState.value.allTasks
            .filter { task ->
                task.title.lowercase().contains(normalizedQuery) ||
                    task.leadName?.lowercase()?.contains(normalizedQuery) == true ||
                    task.statusName?.lowercase()?.contains(normalizedQuery) == true
            }
            .take(25)

        _uiState.value = _uiState.value.copy(
            taskSearchResults = results,
            taskSearchError = null
        )
    }

    fun save(request: CalendarEventCreateRequest) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val event = repository.createEvent(request)

                _uiState.value = CalendarEventCreateUiState(
                    isSaving = false,
                    createdEvent = event,
                    saveSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Unable to create calendar event"
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(
            saveSuccessful = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}
