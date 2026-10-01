package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.CalendarEventUpdateRequest
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.data.repository.CalendarRepository
import com.infynity.leadcrm.data.repository.LeadRepository
import com.infynity.leadcrm.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalendarEventEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val event: CalendarEventResponse? = null,
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false,
    val selectedLead: LeadResponse? = null,
    val isSearchingLeads: Boolean = false,
    val leadSearchResults: List<LeadResponse> = emptyList(),
    val leadSearchError: String? = null,
    val isLoadingTasks: Boolean = false,
    val allTasks: List<TaskResponse> = emptyList(),
    val taskSearchResults: List<TaskResponse> = emptyList(),
    val taskSearchError: String? = null
)

class CalendarEventEditViewModel(
    private val repository: CalendarRepository,
    private val leadRepository: LeadRepository,
    private val taskRepository: TaskRepository,
    private val eventId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarEventEditUiState())
    val uiState: StateFlow<CalendarEventEditUiState> = _uiState.asStateFlow()

    init {
        loadEvent()
        loadTasks()
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

                _uiState.value = _uiState.value.copy(
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

    fun loadLeadForSelection(leadId: Int?, leadName: String?) {
        if (leadId == null) {
            return
        }

        viewModelScope.launch {
            try {
                val detail = leadRepository.getLead(leadId)

                val lead = LeadResponse(
                    id = detail.id,
                    firstName = detail.firstName,
                    lastName = detail.lastName,
                    email = detail.email,
                    phone = detail.phone,
                    phone2 = detail.phone2,
                    infynityCustomer = detail.infynityCustomer,
                    infynityCustomerId = detail.infynityCustomerId,
                    ksebConsumerNumber = detail.ksebConsumerNumber,
                    atCustomerLocation = detail.atCustomerLocation,
                    customerLatitude = detail.customerLatitude,
                    customerLongitude = detail.customerLongitude,
                    customerLocationAccuracy = detail.customerLocationAccuracy,
                    customerLocationCapturedAt = detail.customerLocationCapturedAt,
                    company = detail.company,
                    source = detail.source,
                    placeArea = detail.placeArea,
                    referredBy = detail.referredBy,
                    status = detail.status,
                    assignedToId = detail.assignedToId,
                    teamId = detail.teamId,
                    notes = detail.notes,
                    createdAt = detail.createdAt,
                    updatedAt = detail.updatedAt,
                    convertedAt = detail.convertedAt,
                    lostReason = detail.lostReason,
                    assignedToName = detail.assignedToName,
                    teamName = detail.teamName,
                    productNames = detail.productNames
                )

                _uiState.value = _uiState.value.copy(
                    selectedLead = lead,
                    leadSearchResults = listOf(lead)
                )
            } catch (_: Exception) {
                // Normal Lead search remains available if this lookup fails.
            }
        }
    }

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
                    leadSearchError = e.message
                        ?: "Unable to search leads"
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
                    taskSearchError = e.message
                        ?: "Unable to load tasks"
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
