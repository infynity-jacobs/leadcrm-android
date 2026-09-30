package com.infynity.leadcrm.feature.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.CreateLeadRequest
import com.infynity.leadcrm.core.network.models.LeadAreaResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.SettingOptionResponse
import com.infynity.leadcrm.core.network.models.TeamResponse
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LeadCreateUiState(
    val isSaving: Boolean = false,
    val createdLead: LeadResponse? = null,
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false,
    val isSearchingAreas: Boolean = false,
    val areaSearchResults: List<LeadAreaResponse> = emptyList(),
    val areaSearchError: String? = null,
    val teams: List<TeamResponse> = emptyList(),
    val assignees: List<UserResponse> = emptyList(),
    val referralOptions: List<SettingOptionResponse> = emptyList()
)

class LeadCreateViewModel(
    private val repository: LeadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadCreateUiState())
    val uiState: StateFlow<LeadCreateUiState> = _uiState.asStateFlow()

    fun loadFormOptions() {
        viewModelScope.launch {
            val teams = runCatching { repository.getTeams() }.getOrNull()
            val referrals = runCatching { repository.getReferralOptions() }.getOrNull()
            _uiState.value = _uiState.value.copy(
                teams = teams ?: _uiState.value.teams,
                referralOptions = referrals ?: _uiState.value.referralOptions
            )
        }
    }

    fun loadAssignees(teamId: Int?) {
        viewModelScope.launch {
            val assignees = teamId?.let {
                runCatching { repository.getLeadAssignees(it) }.getOrNull()
            } ?: emptyList()
            _uiState.value = _uiState.value.copy(assignees = assignees)
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

    fun save(request: CreateLeadRequest) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val lead = repository.createLead(request)
                _uiState.value = LeadCreateUiState(
                    createdLead = lead,
                    saveSuccessful = true
                )
            } catch (e: Exception) {
                val message = if (e is HttpException && e.code() == 409) {
                    "A lead with this email or phone already exists."
                } else {
                    e.message ?: "Unable to create lead"
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = message
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccessful = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
