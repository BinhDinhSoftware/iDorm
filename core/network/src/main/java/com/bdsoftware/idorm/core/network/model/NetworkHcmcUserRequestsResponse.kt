package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable
import com.bdsoftware.idorm.core.network.serializer.FlexibleStringSerializer

@Serializable
data class NetworkHcmcUserRequestsResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<NetworkHcmcUserRequestItem>? = null
)

@Serializable
data class NetworkHcmcUserRequestItem(
    val id: Int,
    val service: NetworkHcmcServiceItem? = null,
    val name: String? = null,
    val note: String? = null,
    val request_date: String? = null,
    val approve_user_name: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val approve_content: String? = null,
    val approve_date: String? = null,
    val final_state: String? = null,
    val approve_user_id: Int? = null,
    val histories: List<NetworkHcmcHistoryItem>? = emptyList()
)

@Serializable
data class NetworkHcmcServiceItem(
    val id: Int,
    val name: String? = null,
    val description: String? = null
)
