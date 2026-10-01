package com.infynity.leadcrm.feature.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@Composable
fun ChangePasswordScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !isSaving
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    errorMessage = null
                    successMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current password") },
                singleLine = true,
                enabled = !isSaving,
                visualTransformation = if (currentPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            currentPasswordVisible = !currentPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (currentPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (currentPasswordVisible) {
                                "Hide current password"
                            } else {
                                "Show current password"
                            }
                        )
                    }
                }
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = null
                    successMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New password") },
                singleLine = true,
                enabled = !isSaving,
                visualTransformation = if (newPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            newPasswordVisible = !newPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (newPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (newPasswordVisible) {
                                "Hide new password"
                            } else {
                                "Show new password"
                            }
                        )
                    }
                }
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                    successMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm new password") },
                singleLine = true,
                enabled = !isSaving,
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (confirmPasswordVisible) {
                                "Hide confirmation password"
                            } else {
                                "Show confirmation password"
                            }
                        )
                    }
                }
            )

            Text(
                text = "Password requirements",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = """
                    • At least 12 characters
                    • At least one uppercase letter
                    • At least one lowercase letter
                    • At least one number
                    • At least one special character
                    • Must not contain your username or email
                    • Must not be a common or predictable password
                    • Must not contain long sequential characters
                    • Must not consist of a repeated pattern
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            successMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    errorMessage = null
                    successMessage = null

                    when {
                        currentPassword.isBlank() -> {
                            errorMessage = "Enter your current password."
                        }

                        newPassword.isBlank() -> {
                            errorMessage = "Enter a new password."
                        }

                        newPassword.length < 12 -> {
                            errorMessage =
                                "New password must be at least 12 characters long."
                        }

                        !newPassword.any { it.isUpperCase() } -> {
                            errorMessage =
                                "New password must contain at least one uppercase letter."
                        }

                        !newPassword.any { it.isLowerCase() } -> {
                            errorMessage =
                                "New password must contain at least one lowercase letter."
                        }

                        !newPassword.any { it.isDigit() } -> {
                            errorMessage =
                                "New password must contain at least one number."
                        }

                        !newPassword.any { !it.isLetterOrDigit() } -> {
                            errorMessage =
                                "New password must contain at least one special character."
                        }

                        confirmPassword.isBlank() -> {
                            errorMessage = "Confirm your new password."
                        }

                        newPassword != confirmPassword -> {
                            errorMessage = "New passwords do not match."
                        }

                        else -> {
                            isSaving = true

                            scope.launch {
                                try {
                                    authRepository.changePassword(
                                        currentPassword = currentPassword,
                                        newPassword = newPassword
                                    )

                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""

                                    successMessage =
                                        "Password changed successfully."
                                } catch (e: HttpException) {
                                    val detail = try {
                                        e.response()
                                            ?.errorBody()
                                            ?.string()
                                            ?.let { body ->
                                                JSONObject(body).optString("detail")
                                            }
                                            ?.takeIf { it.isNotBlank() }
                                    } catch (_: Exception) {
                                        null
                                    }

                                    errorMessage = detail
                                        ?: "Unable to change password. Please try again."
                                } catch (_: Exception) {
                                    errorMessage =
                                        "Unable to change password. Please check your connection and try again."
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Change Password")
                }
            }
        }
    }
}
