package com.infynity.leadcrm.core.common

import android.content.Context
import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.NetworkClient
import com.infynity.leadcrm.core.security.SessionManager
import com.infynity.leadcrm.data.repository.AuthRepository
import com.infynity.leadcrm.data.repository.DashboardRepository
import com.infynity.leadcrm.data.repository.LeadRepository

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

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(api)
    }

    val leadRepository: LeadRepository by lazy {
        LeadRepository(api)
    }
}
