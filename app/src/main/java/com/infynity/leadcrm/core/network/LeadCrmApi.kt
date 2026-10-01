package com.infynity.leadcrm.core.network

import com.infynity.leadcrm.core.network.models.LeadDocument
import com.infynity.leadcrm.core.network.models.LeadProduct
import com.infynity.leadcrm.core.network.models.LeadProductCreateRequest
import com.infynity.leadcrm.core.network.models.LeadProductUpdateRequest
import com.infynity.leadcrm.core.network.models.Product
import com.infynity.leadcrm.core.network.models.LeadDocumentRequirementsResponse

import com.infynity.leadcrm.core.network.models.CalendarEventCreateRequest
import com.infynity.leadcrm.core.network.models.CalendarEventListResponse
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.CalendarEventUpdateRequest
import com.infynity.leadcrm.core.network.models.LeadAreaResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.CreateLeadRequest
import com.infynity.leadcrm.core.network.models.LeadListResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.SettingOptionResponse
import com.infynity.leadcrm.core.network.models.TeamResponse
import kotlinx.serialization.json.JsonObject
import com.infynity.leadcrm.core.network.models.TaskListResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import com.infynity.leadcrm.core.network.models.TaskCreateRequest
import com.infynity.leadcrm.core.network.models.TaskUpdateRequest
import com.infynity.leadcrm.core.network.models.TaskStatusResponse
import com.infynity.leadcrm.core.network.models.VoipCallRequest
import com.infynity.leadcrm.core.network.models.VoipCallResponse
import com.infynity.leadcrm.core.network.models.NativeCallStartRequest
import com.infynity.leadcrm.core.network.models.NativeCallStartResponse
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Query

interface LeadCrmApi {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: PasswordChangeRequest
    ): Unit

    @GET("auth/me")
    suspend fun getCurrentUser(): UserResponse

    @PUT("auth/me")
    suspend fun updateCurrentUser(
        @Body request: ProfileUpdateRequest
    ): UserResponse

    @GET("tasks")
    suspend fun getTasks(
        @Query("lead_id") leadId: Int? = null
    ): TaskListResponse

    @GET("task-statuses")
    suspend fun getTaskStatuses(): List<TaskStatusResponse>

    @POST("tasks")
    suspend fun createTask(
        @Body request: TaskCreateRequest
    ): TaskResponse

    @GET("tasks/{taskId}")
    suspend fun getTask(
        @Path("taskId") taskId: Int
    ): TaskResponse

    @PATCH("tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: Int,
        @Body request: TaskUpdateRequest
    ): TaskResponse

    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: Int
    )

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


    @GET("teams")
    suspend fun getTeams(): List<TeamResponse>

    @GET("users")
    suspend fun getUsers(
        @Query("role") role: String? = null,
        @Query("team_id") teamId: Int? = null
    ): List<UserResponse>

    @GET("settings/options")
    suspend fun getSettingOptions(
        @Query("category") category: String,
        @Query("include_inactive") includeInactive: Boolean = false
    ): List<SettingOptionResponse>

    @POST("voip/call")
    suspend fun initiateVoipCall(
        @Body request: VoipCallRequest
    ): VoipCallResponse

    @POST("voip/native-call/start")
    suspend fun startNativeCall(
        @Body request: NativeCallStartRequest
    ): NativeCallStartResponse

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
        @Body request: JsonObject
    ): LeadResponse

    @GET("products")
    suspend fun getProducts(
        @Query("active_only") activeOnly: Boolean = true
    ): List<Product>

    @GET("leads/{leadId}/products")
    suspend fun getLeadProducts(
        @Path("leadId") leadId: Int
    ): List<LeadProduct>

    @POST("leads/{leadId}/products")
    suspend fun addLeadProduct(
        @Path("leadId") leadId: Int,
        @Body request: LeadProductCreateRequest
    ): LeadProduct

    @PUT("leads/{leadId}/products/{itemId}")
    suspend fun updateLeadProduct(
        @Path("leadId") leadId: Int,
        @Path("itemId") itemId: Int,
        @Body request: LeadProductUpdateRequest
    ): LeadProduct

    @retrofit2.http.DELETE("leads/{leadId}/products/{itemId}")
    suspend fun deleteLeadProduct(
        @Path("leadId") leadId: Int,
        @Path("itemId") itemId: Int
    ): Map<String, String>

    @GET("leads/{leadId}/documents/requirements")
    suspend fun getLeadDocumentRequirements(
        @Path("leadId") leadId: Int
    ): LeadDocumentRequirementsResponse

    @GET("leads/{leadId}/documents")
    suspend fun getLeadDocuments(
        @Path("leadId") leadId: Int
    ): List<LeadDocument>

    @Multipart
    @POST("leads/{leadId}/documents")
    suspend fun uploadLeadDocument(
        @Path("leadId") leadId: Int,
        @Part("document_type") documentType: okhttp3.RequestBody,
        @Part file: okhttp3.MultipartBody.Part
    ): LeadDocument

    @GET("leads/{leadId}/documents/{documentId}/view")
    suspend fun viewLeadDocument(
        @Path("leadId") leadId: Int,
        @Path("documentId") documentId: Int
    ): okhttp3.ResponseBody

    @GET("leads/{leadId}/documents/download-all")
    suspend fun downloadAllLeadDocuments(
        @Path("leadId") leadId: Int
    ): okhttp3.ResponseBody

    @retrofit2.http.DELETE("leads/{leadId}")
    suspend fun deleteLead(
        @Path("leadId") leadId: Int
    ): Map<String, String>

}
