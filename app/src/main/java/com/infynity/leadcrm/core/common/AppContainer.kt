package com.infynity.leadcrm.core.common

import android.content.Context
import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.NetworkClient
import com.infynity.leadcrm.core.security.SessionManager
import com.infynity.leadcrm.data.repository.AuthRepository

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    val sessionManager: SessionManager by lazy {
        SessionManager(applicationContext)
    }

    val api: LeadCrmApi by lazy {
        NetworkClient.create(applicationContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(api, sessionManager)
    }
}
