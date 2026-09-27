package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadListResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.UpdateLeadRequest

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

    suspend fun getLead(leadId: Int): LeadDetailResponse {
        return api.getLead(leadId)
    }

    suspend fun updateLead(
        leadId: Int,
        request: UpdateLeadRequest
    ): LeadResponse {
        return api.updateLead(
            leadId = leadId,
            request = request
        )
    }
}
