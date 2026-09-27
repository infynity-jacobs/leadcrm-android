package com.infynity.leadcrm.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskListResponse(
    val total: Int,
    val items: List<TaskResponse>
)

@Serializable
data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    @SerialName("status_id") val statusId: Int,
    val priority: String,
    @SerialName("task_type") val taskType: String,
    @SerialName("lead_id") val leadId: Int? = null,
    @SerialName("assigned_to_id") val assignedToId: Int? = null,
    @SerialName("team_id") val teamId: Int? = null,
    @SerialName("created_by_id") val createdById: Int? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("status_name") val statusName: String? = null,
    @SerialName("status_key") val statusKey: String? = null,
    @SerialName("status_is_closed") val statusIsClosed: Boolean = false,
    @SerialName("lead_name") val leadName: String? = null,
    @SerialName("assigned_to_name") val assignedToName: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("created_by_name") val createdByName: String? = null
)

@Serializable
data class TaskUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    @SerialName("status_id") val statusId: Int? = null,
    val priority: String? = null,
    @SerialName("task_type") val taskType: String? = null,
    @SerialName("lead_id") val leadId: Int? = null,
    @SerialName("assigned_to_id") val assignedToId: Int? = null,
    @SerialName("team_id") val teamId: Int? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("due_date") val dueDate: String? = null
)

@Serializable
data class TaskStatusResponse(
    val id: Int,
    val name: String,
    val key: String,
    val description: String? = null,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_closed") val isClosed: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class CalendarEventListResponse(
    val total: Int,
    val items: List<CalendarEventResponse>
)

@Serializable
data class CalendarEventResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    @SerialName("event_type") val eventType: String,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    @SerialName("all_day") val allDay: Boolean,
    val location: String? = null,
    @SerialName("lead_id") val leadId: Int? = null,
    @SerialName("task_id") val taskId: Int? = null,
    @SerialName("assigned_to_id") val assignedToId: Int? = null,
    @SerialName("team_id") val teamId: Int? = null,
    @SerialName("created_by_id") val createdById: Int? = null,
    val status: String,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("completed_by_id") val completedById: Int? = null,
    @SerialName("completed_by_name") val completedByName: String? = null,
    val outcome: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("lead_name") val leadName: String? = null,
    @SerialName("task_title") val taskTitle: String? = null,
    @SerialName("assigned_to_name") val assignedToName: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("created_by_name") val createdByName: String? = null
)

@Serializable
data class LeadListResponse(
    val total: Int,
    val items: List<LeadResponse>
)

@Serializable
data class LeadResponse(
    val id: Int,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("phone_2") val phone2: String? = null,
    @SerialName("infynity_customer") val infynityCustomer: Boolean = false,
    @SerialName("infynity_customer_id") val infynityCustomerId: String? = null,
    @SerialName("kseb_consumer_number") val ksebConsumerNumber: String? = null,
    @SerialName("at_customer_location") val atCustomerLocation: Boolean = false,
    @SerialName("customer_latitude") val customerLatitude: Double? = null,
    @SerialName("customer_longitude") val customerLongitude: Double? = null,
    @SerialName("customer_location_accuracy") val customerLocationAccuracy: Double? = null,
    @SerialName("customer_location_captured_at") val customerLocationCapturedAt: String? = null,
    val company: String? = null,
    val source: String? = null,
    @SerialName("place_area") val placeArea: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    val status: String,
    @SerialName("assigned_to_id") val assignedToId: Int? = null,
    @SerialName("team_id") val teamId: Int? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("converted_at") val convertedAt: String? = null,
    @SerialName("lost_reason") val lostReason: String? = null,
    @SerialName("assigned_to_name") val assignedToName: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("product_names") val productNames: List<String> = emptyList()
)

@Serializable
data class LeadHistoryResponse(
    val id: Int,
    @SerialName("old_status") val oldStatus: String? = null,
    @SerialName("new_status") val newStatus: String,
    val note: String? = null,
    @SerialName("changed_at") val changedAt: String,
    @SerialName("changed_by_id") val changedById: Int? = null,
    @SerialName("changed_by_name") val changedByName: String? = null
)

@Serializable
data class FollowUpResponse(
    val id: Int,
    @SerialName("lead_id") val leadId: Int,
    @SerialName("staff_id") val staffId: Int? = null,
    @SerialName("staff_name") val staffName: String? = null,
    @SerialName("follow_up_type") val followUpType: String,
    @SerialName("scheduled_at") val scheduledAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    val outcome: String? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class LeadDetailResponse(
    val id: Int,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("phone_2") val phone2: String? = null,
    @SerialName("infynity_customer") val infynityCustomer: Boolean = false,
    @SerialName("infynity_customer_id") val infynityCustomerId: String? = null,
    @SerialName("kseb_consumer_number") val ksebConsumerNumber: String? = null,
    @SerialName("at_customer_location") val atCustomerLocation: Boolean = false,
    @SerialName("customer_latitude") val customerLatitude: Double? = null,
    @SerialName("customer_longitude") val customerLongitude: Double? = null,
    @SerialName("customer_location_accuracy") val customerLocationAccuracy: Double? = null,
    @SerialName("customer_location_captured_at") val customerLocationCapturedAt: String? = null,
    val company: String? = null,
    val source: String? = null,
    @SerialName("place_area") val placeArea: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    val status: String,
    @SerialName("assigned_to_id") val assignedToId: Int? = null,
    @SerialName("team_id") val teamId: Int? = null,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("converted_at") val convertedAt: String? = null,
    @SerialName("lost_reason") val lostReason: String? = null,
    @SerialName("assigned_to_name") val assignedToName: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("product_names") val productNames: List<String> = emptyList(),
    val history: List<LeadHistoryResponse> = emptyList(),
    @SerialName("follow_ups") val followUps: List<FollowUpResponse> = emptyList()
)

@Serializable
data class UpdateLeadRequest(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("phone_2") val phone2: String? = null,
    @SerialName("infynity_customer") val infynityCustomer: Boolean = false,
    @SerialName("infynity_customer_id") val infynityCustomerId: String? = null,
    @SerialName("kseb_consumer_number") val ksebConsumerNumber: String? = null,
    val company: String? = null,
    val source: String? = null,
    @SerialName("place_area") val placeArea: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    val notes: String? = null
)
