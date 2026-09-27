package com.infynity.leadcrm.feature.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeadDetailUiState(
    val isLoading: Boolean = false,
    val lead: LeadDetailResponse? = null,
    val errorMessage: String? = null
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
}
