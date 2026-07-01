package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcNotificationResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<NetworkHcmcNotificationItem>? = null
)

@Serializable
data class NetworkHcmcNotificationItem(
    val id: Int,
    val title: String? = null,
    val body: String? = null,
    val image: String? = null,
    val article: String? = null,
    val is_read: Boolean = false,
    val create_date: String? = null
)

@Serializable
data class NetworkHcmcNotificationDetailRequest(
    val notify_id: String
)

@Serializable
data class NetworkHcmcNotificationReadRequest(
    val user_id: Int,
    val notify_id: Int
)

@Serializable
data class NetworkHcmcNotificationDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: NetworkHcmcNotificationDetailItem? = null
)

@Serializable
data class NetworkHcmcNotificationDetailItem(
    val id: Int,
    val title: String? = null,
    val body: String? = null,
    val image: String? = null,
    val article: String? = null,
    val is_read: Boolean = false,
    val create_date: String? = null,
    val data: NetworkHcmcNotificationDetailInnerData? = null
)

@Serializable
data class NetworkHcmcNotificationDetailInnerData(
    val type: String? = null,
    val id: String? = null
)

@Serializable
data class NetworkHcmcNotificationReadResponse(
    val success: Boolean,
    val message: String? = null
)
