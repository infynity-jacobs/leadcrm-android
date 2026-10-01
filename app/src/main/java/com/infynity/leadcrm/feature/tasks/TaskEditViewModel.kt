package com.infynity.leadcrm.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TeamResponse
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import com.infynity.leadcrm.core.network.models.TaskUpdateRequest
import com.infynity.leadcrm.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val task: TaskResponse? = null,
    val statuses: List<TaskStatusResponse> = emptyList(),
    val teams: List<TeamResponse> = emptyList(),
    val users: List<UserResponse> = emptyList(),
    val errorMessage: String? = null,
    val saveSuccessful: Boolean = false
)

class TaskEditViewModel(
    private val repository: TaskRepository,
    private val taskId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditUiState())
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val task = repository.getTask(taskId)

                val statuses = try {
                    repository.getTaskStatuses()
                        .filter { it.isActive }
                        .sortedWith(
                            compareBy<TaskStatusResponse> { it.displayOrder }
                                .thenBy { it.id }
                        )
                } catch (_: Exception) {
                    _uiState.value.statuses
                }

                val teams = try {
                    repository.getTeams()
                        .filter { it.isActive }
                } catch (_: Exception) {
                    emptyList()
                }

                val users = try {
                    if (task.teamId != null) {
                        repository.getAssignees(task.teamId)
                    } else {
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }

                _uiState.value = TaskEditUiState(
                    isLoading = false,
                    task = task,
                    statuses = statuses,
                    teams = teams,
                    users = users
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load task"
                )
            }
        }
    }

    fun save(request: TaskUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                saveSuccessful = false
            )

            try {
                val updatedTask = repository.updateTask(
                    taskId = taskId,
                    request = request
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    task = updatedTask,
                    saveSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Unable to save task"
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
