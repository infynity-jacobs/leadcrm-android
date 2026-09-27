package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class DashboardData(
    val tasks: List<TaskResponse>,
    val calendarEvents: List<CalendarEventResponse>,
    val leads: List<LeadResponse>
)

class DashboardRepository(
    private val api: LeadCrmApi
) {
    suspend fun loadDashboard(): DashboardData {
        val now = ZonedDateTime.now()
        val startOfToday = now.toLocalDate()
            .atStartOfDay(now.zone)
        val endOfWindow = startOfToday.plusDays(7)

        val startAt = startOfToday.toInstant().toString()
        val endAt = endOfWindow.toInstant().toString()

        val tasks = api.getTasks()
        val calendarEvents = api.getCalendarEvents(
            startAt = startAt,
            endAt = endAt
        )
        val leads = api.getLeads(
            page = 1,
            pageSize = 5
        )

        return DashboardData(
            tasks = tasks.items,
            calendarEvents = calendarEvents.items,
            leads = leads.items
        )
    }
}
