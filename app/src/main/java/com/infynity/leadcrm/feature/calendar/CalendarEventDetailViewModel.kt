package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarEventDetailUiState(
    val isLoading: Boolean = false,
    val event: CalendarEventResponse? = null,
    val errorMessage: String? = null
)

class CalendarEventDetailViewModel(
    private val repository: CalendarRepository,
    private val eventId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarEventDetailUiState())
    val uiState: StateFlow<CalendarEventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val event = repository.getEvent(eventId)

                _uiState.value = CalendarEventDetailUiState(
                    isLoading = false,
                    event = event
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load calendar event"
                )
            }
        }
    }

    fun refresh() {
        loadEvent()
    }
}
