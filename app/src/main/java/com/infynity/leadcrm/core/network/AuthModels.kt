package com.infynity.leadcrm.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String = "bearer",
    val role: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("user_id")
    val userId: Int
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    val role: String,
    @SerialName("team_id")
    val teamId: Int? = null,
    @SerialName("team_memberships")
    val teamMemberships: List<TeamMembershipResponse> = emptyList(),
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class TeamMembershipResponse(
    val id: Int? = null,
    @SerialName("team_id")
    val teamId: Int? = null,
    @SerialName("team_name")
    val teamName: String? = null
)


@Serializable
data class PasswordChangeRequest(
    @SerialName("current_password")
    val currentPassword: String,
    @SerialName("new_password")
    val newPassword: String
)


@Serializable
data class ProfileUpdateRequest(
    @SerialName("full_name")
    val fullName: String,
    val email: String
)
