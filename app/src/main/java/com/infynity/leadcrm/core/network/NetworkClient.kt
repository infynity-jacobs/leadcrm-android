package com.infynity.leadcrm.core.network

import android.content.Context
import com.infynity.leadcrm.core.security.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkClient {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun create(context: Context): LeadCrmApi {
        val sessionManager = SessionManager(context.applicationContext)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(LeadCrmApi::class.java)
    }
}
