package com.infynity.leadcrm.core.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val sku: String? = null,
    val description: String? = null,
    @SerialName("category_id")
    val categoryId: Int? = null,
    val price: Int = 0,
    val currency: String = "INR",
    @SerialName("tax_percent")
    val taxPercent: Int = 0,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("category_name")
    val categoryName: String? = null
)

@Serializable
data class LeadProduct(
    val id: Int,
    @SerialName("lead_id")
    val leadId: Int,
    @SerialName("product_id")
    val productId: Int,
    val quantity: Int = 1,
    @SerialName("interest_status")
    val interestStatus: String = "interested",
    @SerialName("quoted_price")
    val quotedPrice: Int? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("product_name")
    val productName: String? = null,
    val sku: String? = null,
    @SerialName("unit_price")
    val unitPrice: Int? = null,
    val currency: String? = null
)

@Serializable
data class LeadProductCreateRequest(
    @SerialName("product_id")
    val productId: Int,
    val quantity: Int = 1,
    @SerialName("interest_status")
    val interestStatus: String = "interested",
    @SerialName("quoted_price")
    val quotedPrice: Int? = null,
    val notes: String? = null
)

@Serializable
data class LeadProductUpdateRequest(
    val quantity: Int? = null,
    @SerialName("interest_status")
    val interestStatus: String? = null,
    @SerialName("quoted_price")
    val quotedPrice: Int? = null,
    val notes: String? = null
)
