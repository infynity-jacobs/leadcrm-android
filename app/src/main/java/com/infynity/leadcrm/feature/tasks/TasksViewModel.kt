package com.infynity.leadcrm.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import com.infynity.leadcrm.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<TaskResponse> = emptyList(),
    val total: Int = 0,
    val statuses: List<TaskStatusResponse> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusId: Int? = null,
    val selectedPriority: String? = null,
    val selectedTaskType: String? = null,
    val errorMessage: String? = null
)

class TasksViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        val current = _uiState.value

        viewModelScope.launch {
            _uiState.value = current.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = repository.getTasks()

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

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks = response.items,
                    total = response.total,
                    statuses = statuses,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load tasks"
                )
            }
        }
    }

    fun visibleTasks(state: TasksUiState): List<TaskResponse> {
        val query = state.searchQuery.trim().lowercase()

        return state.tasks
            .asSequence()
            .filter { task ->
                state.selectedStatusId == null ||
                    task.statusId == state.selectedStatusId
            }
            .filter { task ->
                state.selectedPriority == null ||
                    task.priority.equals(state.selectedPriority, ignoreCase = true)
            }
            .filter { task ->
                state.selectedTaskType == null ||
                    task.taskType.equals(state.selectedTaskType, ignoreCase = true)
            }
            .filter { task ->
                query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.orEmpty().contains(query, ignoreCase = true) ||
                    task.leadName.orEmpty().contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareBy<TaskResponse> { it.statusIsClosed }
                    .thenBy { it.dueDate == null }
                    .thenBy { it.dueDate }
                    .thenByDescending { it.id }
            )
            .toList()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    fun setStatus(statusId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedStatusId = statusId
        )
    }

    fun setPriority(priority: String?) {
        _uiState.value = _uiState.value.copy(
            selectedPriority = priority
        )
    }

    fun setTaskType(taskType: String?) {
        _uiState.value = _uiState.value.copy(
            selectedTaskType = taskType
        )
    }

    fun refresh() {
        loadTasks()
    }
}
