package com.infynity.leadcrm.core.network

import com.infynity.leadcrm.core.network.models.CalendarEventListResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadListResponse
import com.infynity.leadcrm.core.network.models.TaskListResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
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

    @GET("calendar/events")
    suspend fun getCalendarEvents(
        @Query("start_at") startAt: String,
        @Query("end_at") endAt: String
    ): CalendarEventListResponse

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

}
