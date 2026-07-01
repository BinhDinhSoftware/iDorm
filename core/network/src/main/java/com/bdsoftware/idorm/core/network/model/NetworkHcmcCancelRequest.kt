package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcCancelRequest(
    val request_id: Int,
    val user_id: Int,
    val cancel_reason: String
)
