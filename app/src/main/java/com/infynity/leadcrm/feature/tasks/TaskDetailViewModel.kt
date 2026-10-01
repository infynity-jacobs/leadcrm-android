package com.infynity.leadcrm.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val task: TaskResponse? = null,
    val errorMessage: String? = null,
    val deleteSuccessful: Boolean = false
)

class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val task = repository.getTask(taskId)

                _uiState.value = TaskDetailUiState(
                    isLoading = false,
                    task = task
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load task"
                )
            }
        }
    }

    fun refresh() {
        loadTask()
    }

    fun deleteTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null,
                deleteSuccessful = false
            )

            try {
                repository.deleteTask(taskId)

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccessful = true
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    403 -> "You do not have permission to delete this task."
                    404 -> "This task no longer exists."
                    else -> "Unable to delete task. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Unable to delete task"
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
