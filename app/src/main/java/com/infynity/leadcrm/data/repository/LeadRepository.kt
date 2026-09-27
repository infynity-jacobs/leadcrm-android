package com.infynity.leadcrm.data.repository

import com.infynity.leadcrm.core.network.LeadCrmApi
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadListResponse

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
}
