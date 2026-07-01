package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcReviewRequest(
    val user_id: Int,
    val request_id: Int,
    val rating: String,
    val comments: String
)

@Serializable
data class NetworkHcmcReviewItem(
    val id: Int,
    val request_id: Int? = null,
    val name: String? = null,
    val rating: String? = null,
    val user_id: Int? = null,
    val comments: String? = null,
    val review_date: String? = null,
    val updated_at: String? = null
)
