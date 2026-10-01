package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.LoginResponse
import com.infynity.leadcrm.core.network.PasswordChangeRequest
import com.infynity.leadcrm.core.network.ProfileUpdateRequest
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.security.SessionManager

class AuthRepository(
    private val api: LeadCrmApi,
    private val sessionManager: SessionManager
) {

    suspend fun login(
        username: String,
        password: String
    ): LoginResponse {
        val response = api.login(username, password)
        sessionManager.saveAccessToken(response.accessToken)
        return response
    }

    suspend fun getCurrentUser(): UserResponse {
        return api.getCurrentUser()
    }

    suspend fun updateCurrentUser(
        fullName: String,
        email: String
    ): UserResponse {
        return api.updateCurrentUser(
            ProfileUpdateRequest(
                fullName = fullName,
                email = email
            )
        )
    }


    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        api.changePassword(
            PasswordChangeRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
