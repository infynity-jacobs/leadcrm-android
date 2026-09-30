package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.CreateLeadRequest
import com.infynity.leadcrm.core.network.models.LeadAreaResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadDocument
import com.infynity.leadcrm.core.network.models.LeadDocumentRequirementsResponse
import com.infynity.leadcrm.core.network.models.LeadListResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.SettingOptionResponse
import com.infynity.leadcrm.core.network.models.TeamResponse
import com.infynity.leadcrm.core.network.models.toApiPayload
import com.infynity.leadcrm.core.network.models.UpdateLeadRequest
import com.infynity.leadcrm.core.network.models.VoipCallRequest
import com.infynity.leadcrm.core.network.models.VoipCallResponse
import com.infynity.leadcrm.core.network.models.NativeCallStartRequest
import com.infynity.leadcrm.core.network.models.NativeCallStartResponse

class LeadRepository(
    private val api: LeadCrmApi
) {
    suspend fun getLeads(
        page: Int = 1,
        pageSize: Int = 25,
        search: String? = null,
        status: String? = null
    ): LeadListResponse {
        return api.getLeads(
            page = page,
            pageSize = pageSize,
            search = search,
            status = status
        )
    }

    suspend fun getLeadAreas(
        search: String? = null
    ): List<LeadAreaResponse> {
        return api.getLeadAreas(search = search)
    }

    suspend fun getTeams(): List<TeamResponse> = api.getTeams()

    suspend fun getLeadAssignees(teamId: Int): List<UserResponse> =
        api.getUsers(role = "marketing_staff", teamId = teamId)

    suspend fun getReferralOptions(): List<SettingOptionResponse> =
        api.getSettingOptions(category = "referred_by")

    suspend fun getLead(leadId: Int): LeadDetailResponse {
        return api.getLead(leadId)
    }

    suspend fun updateLead(
        leadId: Int,
        request: UpdateLeadRequest
    ): LeadResponse {
        return api.updateLead(
            leadId = leadId,
            request = request.toApiPayload()
        )
    }

    suspend fun createLead(request: CreateLeadRequest): LeadResponse {
        return api.createLead(request)
    }


    suspend fun initiateVoipCall(
        leadId: Int,
        autoanswer: String = "no"
    ): VoipCallResponse {
        return api.initiateVoipCall(
            VoipCallRequest(
                leadId = leadId,
                autoanswer = autoanswer
            )
        )
    }

    suspend fun startNativeCall(
        leadId: Int
    ): NativeCallStartResponse {
        return api.startNativeCall(
            NativeCallStartRequest(
                leadId = leadId
            )
        )
    }

    suspend fun getLeadDocumentRequirements(
        leadId: Int
    ): LeadDocumentRequirementsResponse {
        return api.getLeadDocumentRequirements(leadId)
    }

    suspend fun getLeadDocuments(
        leadId: Int
    ): List<LeadDocument> {
        return api.getLeadDocuments(leadId)
    }

    suspend fun uploadLeadDocument(
        leadId: Int,
        documentType: okhttp3.RequestBody,
        file: okhttp3.MultipartBody.Part
    ): LeadDocument {
        return api.uploadLeadDocument(
            leadId = leadId,
            documentType = documentType,
            file = file
        )
    }

    suspend fun viewLeadDocument(
        leadId: Int,
        documentId: Int
    ): okhttp3.ResponseBody {
        return api.viewLeadDocument(leadId, documentId)
    }

    suspend fun downloadAllLeadDocuments(
        leadId: Int
    ): okhttp3.ResponseBody {
        return api.downloadAllLeadDocuments(leadId)
    }

    suspend fun deleteLead(leadId: Int): Map<String, String> {
        return api.deleteLead(leadId)
    }
}
