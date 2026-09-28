package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.infynity.leadcrm.data.repository.CalendarRepository
import com.infynity.leadcrm.data.repository.LeadRepository

class CalendarEventCreateViewModelFactory(
    private val repository: CalendarRepository,
    private val leadRepository: LeadRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarEventCreateViewModel::class.java)) {
            return CalendarEventCreateViewModel(repository, leadRepository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
