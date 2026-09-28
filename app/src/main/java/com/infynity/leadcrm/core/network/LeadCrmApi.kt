package com.infynity.leadcrm.core.network

import com.infynity.leadcrm.core.network.models.CalendarEventCreateRequest
import com.infynity.leadcrm.core.network.models.CalendarEventListResponse
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.CalendarEventUpdateRequest
import com.infynity.leadcrm.core.network.models.LeadAreaResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.CreateLeadRequest
import com.infynity.leadcrm.core.network.models.LeadListResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.UpdateLeadRequest
import com.infynity.leadcrm.core.network.models.TaskListResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TaskUpdateRequest
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LeadCrmApi {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("auth/me")
    suspend fun getCurrentUser(): UserResponse

    @GET("tasks")
    suspend fun getTasks(): TaskListResponse

    @GET("task-statuses")
    suspend fun getTaskStatuses(): List<TaskStatusResponse>

    @GET("tasks/{taskId}")
    suspend fun getTask(
        @Path("taskId") taskId: Int
    ): TaskResponse

    @PATCH("tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: Int,
        @Body request: TaskUpdateRequest
    ): TaskResponse

    @GET("calendar/events")
    suspend fun getCalendarEvents(
        @Query("start_at") startAt: String,
        @Query("end_at") endAt: String,
        @Query("team_id") teamId: Int? = null,
        @Query("assigned_to_id") assignedToId: Int? = null,
        @Query("lead_id") leadId: Int? = null,
        @Query("q") query: String? = null
    ): CalendarEventListResponse

    @GET("calendar/events/{eventId}")
    suspend fun getCalendarEvent(
        @Path("eventId") eventId: Int
    ): CalendarEventResponse

    @GET("calendar/tasks/{taskId}/events")
    suspend fun getTaskCalendarEvents(
        @Path("taskId") taskId: Int
    ): CalendarEventListResponse

    @POST("calendar/events")
    suspend fun createCalendarEvent(
        @Body request: CalendarEventCreateRequest
    ): CalendarEventResponse

    @PUT("calendar/events/{eventId}")
    suspend fun updateCalendarEvent(
        @Path("eventId") eventId: Int,
        @Body request: CalendarEventUpdateRequest
    ): CalendarEventResponse

    @retrofit2.http.DELETE("calendar/events/{eventId}")
    suspend fun deleteCalendarEvent(
        @Path("eventId") eventId: Int
    ): Map<String, String>

    @GET("lead-areas")
    suspend fun getLeadAreas(
        @Query("active_only") activeOnly: Boolean = true,
        @Query("search") search: String? = null
    ): List<LeadAreaResponse>

    @GET("leads")
    suspend fun getLeads(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 25,
        @Query("status") status: String? = null,
        @Query("team_id") teamId: Int? = null,
        @Query("assigned_to_id") assignedToId: Int? = null,
        @Query("source") source: String? = null,
        @Query("place_area") placeArea: String? = null,
        @Query("referred_by") referredBy: String? = null,
        @Query("search") search: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): LeadListResponse

    @GET("leads/{leadId}")
    suspend fun getLead(
        @Path("leadId") leadId: Int
    ): LeadDetailResponse

    @POST("leads")
    suspend fun createLead(
        @Body request: CreateLeadRequest
    ): LeadResponse

    @PUT("leads/{leadId}")
    suspend fun updateLead(
        @Path("leadId") leadId: Int,
        @Body request: UpdateLeadRequest
    ): LeadResponse

    @retrofit2.http.DELETE("leads/{leadId}")
    suspend fun deleteLead(
        @Path("leadId") leadId: Int
    ): Map<String, String>

}
