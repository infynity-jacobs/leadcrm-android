package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventCreateRequest
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.data.repository.CalendarRepository
import com.infynity.leadcrm.data.repository.LeadRepository
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
    val leadSearchError: String? = null
)

class CalendarEventCreateViewModel(
    private val repository: CalendarRepository,
    private val leadRepository: LeadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarEventCreateUiState())
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
