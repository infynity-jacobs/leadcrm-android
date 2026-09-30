package com.infynity.leadcrm.feature.leads

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.infynity.leadcrm.data.repository.LeadRepository
import com.infynity.leadcrm.data.repository.TaskRepository

class LeadDetailViewModelFactory(
    private val repository: LeadRepository,
    private val taskRepository: TaskRepository,
    private val leadId: Int,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadDetailViewModel::class.java)) {
            return LeadDetailViewModel(
                repository,
                taskRepository,
                leadId,
                context.applicationContext
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
