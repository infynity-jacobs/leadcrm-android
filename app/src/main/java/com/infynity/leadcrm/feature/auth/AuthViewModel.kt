package com.infynity.leadcrm.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object CheckingSession : AuthUiState
    data object LoggedOut : AuthUiState
    data object Loading : AuthUiState
    data class LoggedIn(val user: UserResponse) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AuthUiState>(AuthUiState.CheckingSession)

    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            if (!repository.isLoggedIn()) {
                _uiState.value = AuthUiState.LoggedOut
                return@launch
            }

            try {
                val user = repository.getCurrentUser()
                _uiState.value = AuthUiState.LoggedIn(user)
            } catch (e: Exception) {
                repository.logout()
                _uiState.value = AuthUiState.LoggedOut
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value =
                AuthUiState.Error("Username and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                repository.login(username, password)
                val user = repository.getCurrentUser()
                _uiState.value = AuthUiState.LoggedIn(user)
            } catch (e: Exception) {
                repository.logout()
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
