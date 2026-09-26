package com.infynity.leadcrm.core.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "bearer"
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val fullName: String? = null,
    val email: String? = null,
    val role: String? = null
)
