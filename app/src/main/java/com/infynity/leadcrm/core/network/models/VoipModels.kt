package com.infynity.leadcrm.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoipCallRequest(
    @SerialName("lead_id") val leadId: Int,
    val autoanswer: String = "no"
)

@Serializable
data class VoipCallResponse(
    val ok: Boolean,
    val provider: String? = null,
    val message: String? = null,
    @SerialName("callid") val callId: String? = null,
    val extension: String? = null
)

@Serializable
data class NativeCallStartRequest(
    @SerialName("lead_id") val leadId: Int
)

@Serializable
data class NativeCallStartResponse(
    val ok: Boolean,
    @SerialName("session_id") val sessionId: Int? = null,
    @SerialName("lead_id") val leadId: Int? = null,
    val phone: String? = null,
    @SerialName("sip_username") val sipUsername: String? = null,
    @SerialName("sip_password") val sipPassword: String? = null,
    @SerialName("sip_domain") val sipDomain: String? = null,
    @SerialName("sip_port") val sipPort: Int? = null,
    @SerialName("sip_transport") val sipTransport: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)
