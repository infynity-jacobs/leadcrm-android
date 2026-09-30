package com.infynity.leadcrm.core.voip

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.Address
import org.linphone.core.AuthInfo
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.CallParams
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.Transports

class NativeSipManager(private val context: Context) {

    enum class State {
        Idle,
        Starting,
        Registering,
        Registered,
        Calling,
        Connected,
        Ending,
        Error
    }

    data class SipConfig(
        val username: String,
        val password: String,
        val domain: String,
        val port: Int,
        val transport: String = "tls"
    )

    private val _state = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _speakerEnabled = MutableStateFlow(false)
    val speakerEnabled: StateFlow<Boolean> = _speakerEnabled.asStateFlow()

    private var core: Core? = null
    private var account: Account? = null
    private var currentCall: Call? = null
    private var sipConfig: SipConfig? = null
    private var pendingDestination: String? = null
    private var registrationState: RegistrationState = RegistrationState.None
    private var normalAudioDevice: AudioDevice? = null

    private val coreListener = object : CoreListenerStub() {

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            registrationState = state

            when (state) {
                RegistrationState.Progress,
                RegistrationState.Refreshing -> {
                    _state.value = State.Registering
                    _message.value = message.ifBlank { "Registering SIP account..." }
                }

                RegistrationState.Ok -> {
                    _state.value = State.Registered
                    _message.value = "SIP registered"

                    val destination = pendingDestination
                    if (destination != null && currentCall == null) {
                        pendingDestination = null
                        placeCall(destination)
                    }
                }

                RegistrationState.Failed -> {
                    _state.value = State.Error
                    _message.value = message.ifBlank { "SIP registration failed" }
                }

                RegistrationState.Cleared,
                RegistrationState.None -> {
                    if (_state.value != State.Ending) {
                        _state.value = State.Idle
                    }
                }
            }
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State,
            message: String
        ) {
            when (state) {
                Call.State.OutgoingInit,
                Call.State.OutgoingProgress,
                Call.State.OutgoingRinging -> {
                    currentCall = call
                    _state.value = State.Calling
                    _message.value = message.ifBlank { "Calling..." }
                }

                Call.State.Connected,
                Call.State.StreamsRunning -> {
                    currentCall = call
                    if (normalAudioDevice == null && !_speakerEnabled.value) {
                        normalAudioDevice = call.outputAudioDevice
                    }
                    _state.value = State.Connected
                    _message.value = "Call connected"
                }

                Call.State.End,
                Call.State.Error,
                Call.State.Released -> {
                    if (currentCall === call) {
                        currentCall = null
                    }
                    normalAudioDevice = null
                    _speakerEnabled.value = false

                    try {
                        core.activateAudioSession(false)
                    } catch (_: Throwable) {
                    }

                    _state.value = State.Registered
                    _message.value = message.ifBlank { "Call ended" }
                }

                else -> {
                    _message.value = message.ifBlank { state.name }
                }
            }
        }
    }

    @Synchronized
    fun start(config: SipConfig) {
        if (core != null) {
            return
        }

        sipConfig = config
        _state.value = State.Starting
        _message.value = null

        try {
            val factory = Factory.instance()

            val newCore = factory.createCore(null, null, context.applicationContext)
            core = newCore
            newCore.addListener(coreListener)

            configureTransports(newCore, config)
            configureAccount(newCore, factory, config)

            val result = newCore.start()
            if (result != 0) {
                throw IllegalStateException("Linphone Core start failed: $result")
            }

            _state.value = State.Registering
            _message.value = "Starting SIP registration..."
        } catch (t: Throwable) {
            _state.value = State.Error
            _message.value = t.message ?: "Unable to start SIP"
            stopInternal()
        }
    }

    @Synchronized
    fun call(phone: String): Boolean {
        val destination = phone.filter(Char::isDigit)
        if (destination.isBlank()) {
            _message.value = "Invalid phone number"
            return false
        }

        if (core == null) {
            _message.value = "SIP is not started"
            return false
        }

        if (account == null) {
            _message.value = "SIP account is not ready"
            return false
        }

        if (currentCall != null) {
            _message.value = "A SIP call is already active"
            return false
        }

        pendingDestination = destination

        return if (registrationState == RegistrationState.Ok) {
            pendingDestination = null
            placeCall(destination)
            true
        } else {
            _state.value = State.Registering
            _message.value = "Registering SIP account..."
            true
        }
    }

    @Synchronized
    private fun placeCall(destination: String) {
        val currentCore = core ?: run {
            _state.value = State.Error
            _message.value = "SIP is not started"
            return
        }

        if (account == null) {
            _state.value = State.Error
            _message.value = "SIP account is not ready"
            return
        }

        try {
            currentCore.configureAudioSession()
            currentCore.activateAudioSession(true)
            currentCore.isMicEnabled = true

            val address = currentCore.createAddress(
                "sip:$destination@${sipConfig?.domain}"
            ) ?: run {
                _state.value = State.Error
                _message.value = "Unable to create SIP destination"
                return
            }

            val params: CallParams = currentCore.createCallParams(null)
                ?: run {
                    _state.value = State.Error
                    _message.value = "Unable to create call parameters"
                    return
                }

            params.isVideoEnabled = false
            params.isAudioEnabled = true
            params.isMicEnabled = true

            val call = currentCore.inviteAddressWithParams(address, params)
                ?: run {
                    _state.value = State.Error
                    _message.value = "Unable to start SIP call"
                    return
                }

            currentCall = call
            _state.value = State.Calling
            _message.value = "Calling..."
        } catch (t: Throwable) {
            _state.value = State.Error
            _message.value = t.message ?: "SIP call failed"
        }
    }

    @Synchronized
    fun hangUp() {
        val call = currentCall
        if (call == null) {
            return
        }

        _state.value = State.Ending
        _message.value = "Ending call..."

        try {
            call.terminate()
        } catch (t: Throwable) {
            _message.value = t.message ?: "Unable to end call"
        }
    }

    @Synchronized
    fun toggleSpeaker(): Boolean {
        val call = currentCall ?: return false
        if (_state.value != State.Connected) return false

        val currentCore = core ?: return false
        return try {
            if (_speakerEnabled.value) {
                val normalDevice = normalAudioDevice
                    ?: currentCore.audioDevices.firstOrNull {
                        it.type == AudioDevice.Type.Earpiece
                    }
                    ?: return false
                call.outputAudioDevice = normalDevice
                _speakerEnabled.value = false
            } else {
                val speaker = currentCore.audioDevices.firstOrNull {
                    it.type == AudioDevice.Type.Speaker
                } ?: run {
                    _message.value = "Speaker audio route is unavailable"
                    return false
                }
                if (normalAudioDevice == null) {
                    normalAudioDevice = call.outputAudioDevice
                        ?: currentCore.audioDevices.firstOrNull {
                            it.type == AudioDevice.Type.Earpiece
                        }
                }
                call.outputAudioDevice = speaker
                _speakerEnabled.value = true
            }
            true
        } catch (t: Throwable) {
            _message.value = t.message ?: "Unable to change audio route"
            false
        }
    }

    @Synchronized
    fun stop() {
        _state.value = State.Ending
        stopInternal()
        _state.value = State.Idle
        _message.value = null
    }

    private fun configureTransports(
        core: Core,
        config: SipConfig
    ) {
        val transports: Transports = core.transports
        transports.udpPort = 0
        transports.tcpPort = 0
        transports.tlsPort = config.port
        transports.dtlsPort = 0
        core.transports = transports
    }

    private fun configureAccount(
        core: Core,
        factory: Factory,
        config: SipConfig
    ) {
        val identity: Address = core.createAddress(
            "sip:${config.username}@${config.domain}"
        ) ?: throw IllegalStateException("Unable to create SIP identity")

        val server: Address = core.createAddress(
            "sip:${config.domain}:${config.port};transport=tls"
        ) ?: throw IllegalStateException("Unable to create SIP server")

        val authInfo: AuthInfo = factory.createAuthInfo(
            config.username,
            null,
            config.password,
            null,
            null,
            config.domain
        )

        core.addAuthInfo(authInfo)

        val params: AccountParams = core.createAccountParams()
        params.identityAddress = identity
        params.serverAddress = server
        params.transport = TransportType.Tls
        params.isRegisterEnabled = true

        val newAccount = core.createAccount(params)
            ?: throw IllegalStateException("Unable to create SIP account")

        core.addAccount(newAccount)
        core.defaultAccount = newAccount
        account = newAccount
    }

    private fun stopInternal() {
        currentCall = null
        normalAudioDevice = null
        _speakerEnabled.value = false
        pendingDestination = null
        registrationState = RegistrationState.None

        val currentCore = core
        if (currentCore != null) {
            try {
                currentCore.removeListener(coreListener)
            } catch (_: Throwable) {
            }

            try {
                currentCore.stop()
            } catch (_: Throwable) {
            }
        }

        account = null
        core = null
        sipConfig = null
    }
}
