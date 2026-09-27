package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.CalendarEventCreateRequest
import com.infynity.leadcrm.core.network.models.CalendarEventListResponse
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.CalendarEventUpdateRequest

class CalendarRepository(
    private val api: LeadCrmApi
) {

    suspend fun getEvents(
        startAt: String,
        endAt: String,
        teamId: Int? = null,
        assignedToId: Int? = null,
        leadId: Int? = null,
        query: String? = null
    ): CalendarEventListResponse {
        return api.getCalendarEvents(
            startAt = startAt,
            endAt = endAt,
            teamId = teamId,
            assignedToId = assignedToId,
            leadId = leadId,
            query = query
        )
    }

    suspend fun getEvent(eventId: Int): CalendarEventResponse {
        return api.getCalendarEvent(eventId)
    }

    suspend fun getTaskEvents(taskId: Int): CalendarEventListResponse {
        return api.getTaskCalendarEvents(taskId)
    }

    suspend fun createEvent(
        request: CalendarEventCreateRequest
    ): CalendarEventResponse {
        return api.createCalendarEvent(request)
    }

    suspend fun updateEvent(
        eventId: Int,
        request: CalendarEventUpdateRequest
    ): CalendarEventResponse {
        return api.updateCalendarEvent(eventId, request)
    }

    suspend fun deleteEvent(eventId: Int): Map<String, String> {
        return api.deleteCalendarEvent(eventId)
    }
}
