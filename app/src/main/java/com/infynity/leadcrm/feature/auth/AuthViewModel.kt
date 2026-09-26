package com.infynity.leadcrm.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.LoginResponse
import com.infynity.leadcrm.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object LoggedOut : AuthUiState
    data object Loading : AuthUiState
    data class LoggedIn(val user: LoginResponse) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.LoggedOut)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Username and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val response = repository.login(username, password)
                _uiState.value = AuthUiState.LoggedIn(response)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    e.message ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState.LoggedOut
    }
}
