package com.infynity.leadcrm.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeadDocumentRequirement(
    @SerialName("document_type")
    val documentType: String,
    val name: String,
    val description: String? = null,
    @SerialName("allow_multiple")
    val multipleAllowed: Boolean = false,
    @SerialName("is_required")
    val required: Boolean = false,
    @SerialName("display_order")
    val displayOrder: Int = 0
) {
    val displayName: String
        get() = name
}

@Serializable
data class LeadDocument(
    val id: Int,
    @SerialName("lead_id")
    val leadId: Int? = null,
    @SerialName("document_type")
    val documentType: String,
    @SerialName("original_filename")
    val originalFilename: String,
    @SerialName("content_type")
    val contentType: String? = null,
    @SerialName("file_size")
    val size: Long? = null,
    @SerialName("uploaded_by")
    val uploadedBy: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class LeadDocumentRequirementsResponse(
    val requirements: List<LeadDocumentRequirement> = emptyList(),
    @SerialName("existing_documents")
    val existingDocuments: List<LeadDocument> = emptyList()
)
