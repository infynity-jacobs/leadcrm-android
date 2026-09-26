package com.infynity.leadcrm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.infynity.leadcrm.feature.auth.AuthUiState
import com.infynity.leadcrm.feature.auth.AuthViewModel
import com.infynity.leadcrm.feature.auth.AuthViewModelFactory
import com.infynity.leadcrm.feature.auth.LoginScreen
import com.infynity.leadcrm.core.navigation.AppShell

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
                onLogin = authViewModel::login,
                onLogout = authViewModel::logout
            )
        }
    }
}

@Composable
fun LeadCRMApp(
    uiState: AuthUiState,
    onLogin: (username: String, password: String) -> Unit,
    onLogout: () -> Unit
) {
    MaterialTheme {
        when (uiState) {
            AuthUiState.CheckingSession -> {
                LoadingScreen()
            }

            AuthUiState.LoggedOut -> {
                LoginScreen(
                    uiState = uiState,
                    onLogin = onLogin
                )
            }

            AuthUiState.Loading -> {
                LoginScreen(
                    uiState = uiState,
                    onLogin = onLogin
                )
            }

            is AuthUiState.Error -> {
                LoginScreen(
                    uiState = uiState,
                    onLogin = onLogin
                )
            }

            is AuthUiState.LoggedIn -> {
                AppShell(
                    user = uiState.user,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Text(
            text = "Checking session...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
