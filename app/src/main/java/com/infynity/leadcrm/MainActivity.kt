package com.infynity.leadcrm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.infynity.leadcrm.feature.auth.AuthUiState
import com.infynity.leadcrm.feature.auth.AuthViewModel
import com.infynity.leadcrm.feature.auth.AuthViewModelFactory
import com.infynity.leadcrm.feature.auth.LoginScreen

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            (application as LeadCrmApplication)
                .appContainer
                .authRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by authViewModel.uiState.collectAsState()

            LeadCRMApp(
                uiState = uiState,
                onLogin = authViewModel::login
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun LeadCRMApp(
    uiState: AuthUiState,
    onLogin: (username: String, password: String) -> Unit
) {
    androidx.compose.material3.MaterialTheme {
        LoginScreen(
            uiState = uiState,
            onLogin = onLogin
        )
    }
}
