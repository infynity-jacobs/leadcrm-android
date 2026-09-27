package com.infynity.leadcrm.feature.leads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.infynity.leadcrm.data.repository.LeadRepository

class LeadEditViewModelFactory(
    private val repository: LeadRepository,
    private val leadId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadEditViewModel::class.java)) {
            return LeadEditViewModel(repository, leadId) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
