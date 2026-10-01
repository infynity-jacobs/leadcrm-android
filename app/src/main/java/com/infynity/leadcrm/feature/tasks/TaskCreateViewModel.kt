package com.infynity.leadcrm.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.TaskCreateRequest
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import com.infynity.leadcrm.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskCreateUiState(
    val isLoadingStatuses: Boolean = true,
    val isSaving: Boolean = false,
    val statuses: List<TaskStatusResponse> = emptyList(),
    val createdTask: TaskResponse? = null,
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false
)

class TaskCreateViewModel(
    private val repository: TaskRepository,
    private val leadId: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskCreateUiState())
    val uiState: StateFlow<TaskCreateUiState> = _uiState.asStateFlow()

    init {
        loadTaskStatuses()
    }

    fun loadTaskStatuses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingStatuses = true,
                errorMessage = null
            )

            try {
                val statuses = repository.getTaskStatuses()
                    .filter { it.isActive }
                    .sortedWith(
                        compareBy<TaskStatusResponse> { it.displayOrder }
                            .thenBy { it.id }
                    )

                _uiState.value = _uiState.value.copy(
                    isLoadingStatuses = false,
                    statuses = statuses,
                    errorMessage = if (statuses.isEmpty()) {
                        "No active task statuses are available"
                    } else {
                        null
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingStatuses = false,
                    errorMessage = e.message ?: "Unable to load task statuses"
                )
            }
        }
    }

    fun save(request: TaskCreateRequest) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val task = repository.createTask(
                    if (leadId != null) {
                        request.copy(leadId = leadId)
                    } else {
                        request
                    }
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    createdTask = task,
                    saveSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Unable to create task"
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
