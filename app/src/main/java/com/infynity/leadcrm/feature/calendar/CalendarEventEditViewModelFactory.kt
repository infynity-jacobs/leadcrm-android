package com.infynity.leadcrm.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.infynity.leadcrm.data.repository.CalendarRepository
import com.infynity.leadcrm.data.repository.LeadRepository
import com.infynity.leadcrm.data.repository.TaskRepository

class CalendarEventEditViewModelFactory(
    private val repository: CalendarRepository,
    private val leadRepository: LeadRepository,
    private val taskRepository: TaskRepository,
    private val eventId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarEventEditViewModel::class.java)) {
            return CalendarEventEditViewModel(
                repository,
                leadRepository,
                taskRepository,
                eventId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
