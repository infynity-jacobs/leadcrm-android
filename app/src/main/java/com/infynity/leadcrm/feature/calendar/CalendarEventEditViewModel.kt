package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.CalendarEventUpdateRequest
import com.infynity.leadcrm.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarEventEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val event: CalendarEventResponse? = null,
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false
)

class CalendarEventEditViewModel(
    private val repository: CalendarRepository,
    private val eventId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarEventEditUiState())
    val uiState: StateFlow<CalendarEventEditUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val event = repository.getEvent(eventId)

                _uiState.value = CalendarEventEditUiState(
                    isLoading = false,
                    event = event
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to load calendar event"
                )
            }
        }
    }

    fun save(request: CalendarEventUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val updatedEvent = repository.updateEvent(
                    eventId = eventId,
                    request = request
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    event = updatedEvent,
                    saveSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                        ?: "Unable to save calendar event"
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
