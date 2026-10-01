package com.infynity.leadcrm.feature.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LeadsUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val leads: List<LeadResponse> = emptyList(),
    val total: Int = 0,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val selectedStatus: String? = null,
    val errorMessage: String? = null
)

class LeadsViewModel(
    private val repository: LeadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadsUiState())
    val uiState: StateFlow<LeadsUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        loadLeads()
    }

    fun loadLeads() {
        val current = _uiState.value

        viewModelScope.launch {
            _uiState.value = current.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = repository.getLeads(
                    page = 1,
                    pageSize = 25,
                    search = current.searchQuery.takeIf { it.isNotBlank() },
                    status = current.selectedStatus
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    leads = response.items,
                    total = response.total,
                    currentPage = 1,
                    hasMore = response.items.size < response.total,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load leads"
                )
            }
        }
    }

    fun loadMoreLeads() {
        val current = _uiState.value

        if (current.isLoading ||
            current.isLoadingMore ||
            !current.hasMore
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(
                isLoadingMore = true
            )

            try {
                val nextPage = current.currentPage + 1

                val response = repository.getLeads(
                    page = nextPage,
                    pageSize = 25,
                    search = current.searchQuery
                        .takeIf { it.isNotBlank() },
                    status = current.selectedStatus
                )

                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    leads = current.leads + response.items,
                    currentPage = nextPage,
                    hasMore = (current.leads.size + response.items.size)
                        < response.total
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = e.message
                        ?: "Unable to load more leads"
                )
            }
        }
    }


    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )

        searchJob?.cancel()

        val cleanQuery = query.trim()

        if (cleanQuery.length in 1..2) {
            return
        }

        searchJob = viewModelScope.launch {
            delay(400L)
            loadLeads()
        }
    }

    fun search() {
        loadLeads()
    }

    fun setStatus(status: String?) {
        _uiState.value = _uiState.value.copy(
            selectedStatus = status
        )
        loadLeads()
    }

    fun refresh() {
        loadLeads()
    }
}
