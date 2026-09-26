package com.infynity.leadcrm.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private companion object {
        const val PREFS_NAME = "leadcrm_secure_session"
        const val KEY_ACCESS_TOKEN = "access_token"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getAccessToken(): String? =
        preferences.getString(KEY_ACCESS_TOKEN, null)

    fun clearSession() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }

    fun isLoggedIn(): Boolean =
        !getAccessToken().isNullOrBlank()
}
