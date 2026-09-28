package com.infynity.leadcrm.feature.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LeadDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val lead: LeadDetailResponse? = null,
    val errorMessage: String? = null,
    val deleteSuccessful: Boolean = false
)

class LeadDetailViewModel(
    private val repository: LeadRepository,
    private val leadId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadDetailUiState())
    val uiState: StateFlow<LeadDetailUiState> = _uiState.asStateFlow()

    init {
        loadLead()
    }

    fun loadLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val lead = repository.getLead(leadId)

                _uiState.value = LeadDetailUiState(
                    isLoading = false,
                    lead = lead
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load lead"
                )
            }
        }
    }

    fun refresh() {
        loadLead()
    }

    fun deleteLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null,
                deleteSuccessful = false
            )

            try {
                repository.deleteLead(leadId)

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccessful = true
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    403 -> "You do not have permission to delete this lead."
                    404 -> "This lead no longer exists."
                    else -> "Unable to delete lead. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Unable to delete lead"
                )
            }
        }
    }

    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(
            deleteSuccessful = false
        )
    }
}
