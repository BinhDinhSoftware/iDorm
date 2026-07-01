package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcComplaintItem(
    val id: Int,
    val request_id: Int? = null,
    val user_id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val image_ids: List<Int>? = emptyList(),
    val complaint_date: String? = null
)
