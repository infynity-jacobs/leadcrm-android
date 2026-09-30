package com.infynity.leadcrm.feature.leads

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.voip.NativeSipManager
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LeadDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isCalling: Boolean = false,
    val isNativeCalling: Boolean = false,
    val nativeCallState: NativeSipManager.State = NativeSipManager.State.Idle,
    val nativeCallMessage: String? = null,
    val isSpeakerEnabled: Boolean = false,
    val lead: LeadDetailResponse? = null,
    val errorMessage: String? = null,
    val callMessage: String? = null,
    val callSuccessful: Boolean = false,
    val deleteSuccessful: Boolean = false
)

class LeadDetailViewModel(
    private val repository: LeadRepository,
    private val leadId: Int,
    appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadDetailUiState())
    val uiState: StateFlow<LeadDetailUiState> = _uiState.asStateFlow()

    private val nativeSipManager = NativeSipManager(appContext.applicationContext)

    init {
        viewModelScope.launch {
            nativeSipManager.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    nativeCallState = state,
                    isNativeCalling = state == NativeSipManager.State.Calling ||
                        state == NativeSipManager.State.Connected ||
                        state == NativeSipManager.State.Ending
                )
            }
        }

        viewModelScope.launch {
            nativeSipManager.message.collect { message ->
                _uiState.value = _uiState.value.copy(
                    nativeCallMessage = message
                )
            }
        }

        viewModelScope.launch {
            nativeSipManager.speakerEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isSpeakerEnabled = enabled)
            }
        }

        loadLead()
    }

    fun loadLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val lead = repository.getLead(leadId)

                _uiState.value = LeadDetailUiState(
                    isLoading = false,
                    lead = lead
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load lead"
                )
            }
        }
    }

    fun refresh() {
        loadLead()
    }

    fun initiateNativeCall() {
        if (_uiState.value.isNativeCalling) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                nativeCallMessage = null
            )

            try {
                val response = repository.startNativeCall(leadId)

                val phone = response.phone
                if (phone.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        nativeCallMessage = "No dialable phone number was returned."
                    )
                    return@launch
                }

                val sipConfig = NativeSipManager.SipConfig(
                    username = response.sipUsername.orEmpty(),
                    password = response.sipPassword.orEmpty(),
                    domain = response.sipDomain.orEmpty(),
                    port = response.sipPort ?: 5063,
                    transport = response.sipTransport ?: "tls"
                )

                nativeSipManager.start(sipConfig)
                nativeSipManager.call(phone)

            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    nativeCallMessage = "Native call failed: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    nativeCallMessage = e.message ?: "Unable to start native call"
                )
            }
        }
    }

    fun endNativeCall() {
        nativeSipManager.hangUp()
    }

    fun toggleNativeSpeaker() {
        nativeSipManager.toggleSpeaker()
    }

    override fun onCleared() {
        nativeSipManager.stop()
        super.onCleared()
    }

    fun initiateVoipCall() {
        if (_uiState.value.isCalling) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCalling = true,
                callMessage = null,
                callSuccessful = false
            )

            try {
                val response = repository.initiateVoipCall(leadId)

                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    callMessage = response.message ?: "Call initiated.",
                    callSuccessful = response.ok
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "This lead does not have a valid phone number."
                    403 -> "You do not have permission to call this lead."
                    404 -> "This lead no longer exists."
                    409 -> "VOIP is not available for your CRM account."
                    502 -> "Unable to initiate the VOIP call. Please try again."
                    else -> "Unable to initiate VOIP call. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    callMessage = message,
                    callSuccessful = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    callMessage = e.message ?: "Unable to initiate VOIP call.",
                    callSuccessful = false
                )
            }
        }
    }

    fun deleteLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null,
                deleteSuccessful = false
            )

            try {
                repository.deleteLead(leadId)

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccessful = true
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    403 -> "You do not have permission to delete this lead."
                    404 -> "This lead no longer exists."
                    else -> "Unable to delete lead. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Unable to delete lead"
                )
            }
        }
    }

    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(
            deleteSuccessful = false
        )
    }
}
