package com.infynity.leadcrm.core.network

import com.infynity.leadcrm.core.network.models.CalendarEventListResponse
import com.infynity.leadcrm.core.network.models.LeadListResponse
import com.infynity.leadcrm.core.network.models.TaskListResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
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
        @Query("page_size") pageSize: Int = 5
    ): LeadListResponse
}
