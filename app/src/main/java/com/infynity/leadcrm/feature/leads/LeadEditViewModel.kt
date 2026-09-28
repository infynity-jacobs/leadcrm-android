package com.infynity.leadcrm.feature.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.LeadAreaResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.UpdateLeadRequest
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LeadEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val lead: LeadDetailResponse? = null,
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false,
    val isSearchingAreas: Boolean = false,
    val areaSearchResults: List<LeadAreaResponse> = emptyList(),
    val areaSearchError: String? = null
)

class LeadEditViewModel(
    private val repository: LeadRepository,
    private val leadId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadEditUiState())
    val uiState: StateFlow<LeadEditUiState> = _uiState.asStateFlow()

    init {
        loadLead()
    }

    fun loadLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val lead = repository.getLead(leadId)

                _uiState.value = LeadEditUiState(
                    isLoading = false,
                    lead = lead
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    403 -> "You do not have permission to edit this lead."
                    404 -> "This lead no longer exists."
                    else -> "Unable to load lead. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load lead"
                )
            }
        }
    }

    fun searchAreas(query: String) {
        val cleanQuery = query.trim()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingAreas = true,
                areaSearchResults = emptyList(),
                areaSearchError = null
            )

            try {
                val areas = repository.getLeadAreas(
                    search = cleanQuery.ifBlank { null }
                )

                _uiState.value = _uiState.value.copy(
                    isSearchingAreas = false,
                    areaSearchResults = areas,
                    areaSearchError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearchingAreas = false,
                    areaSearchResults = emptyList(),
                    areaSearchError = e.message ?: "Unable to load Areas"
                )
            }
        }
    }

    fun save(request: UpdateLeadRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                repository.updateLead(
                    leadId = leadId,
                    request = request
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccessful = true
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    403 -> "You do not have permission to edit this lead."
                    404 -> "This lead no longer exists."
                    409 -> "A lead with this email or phone already exists."
                    else -> "Unable to save lead. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Unable to save lead"
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
