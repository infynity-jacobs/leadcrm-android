package com.infynity.leadcrm.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.infynity.leadcrm.data.repository.TaskRepository

class TaskCreateViewModelFactory(
    private val repository: TaskRepository,
    private val leadId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskCreateViewModel::class.java)) {
            return TaskCreateViewModel(repository, leadId) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
