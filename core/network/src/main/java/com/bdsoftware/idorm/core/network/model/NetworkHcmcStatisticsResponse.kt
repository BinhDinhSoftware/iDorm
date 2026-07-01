package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcStatisticsResponse(
    val success: Boolean,
    val message: String? = null,
    val data: NetworkHcmcStatisticsData? = null
)

@Serializable
data class NetworkHcmcStatisticsData(
    val summary: NetworkHcmcStatisticsSummary? = null,
    val details: NetworkHcmcStatisticsDetails? = null
)

@Serializable
data class NetworkHcmcStatisticsSummary(
    val new_requests: Int = 0,
    val processing_requests: Int = 0,
    val overdue_requests: Int = 0,
    val warning_requests: Int = 0
)

@Serializable
data class NetworkHcmcStatisticsDetails(
    val new_requests: List<NetworkHcmcStatisticsDetailItem> = emptyList(),
    val processing_requests: List<NetworkHcmcStatisticsDetailItem> = emptyList(),
    val overdue_requests: List<NetworkHcmcStatisticsDetailItem> = emptyList(),
    val warning_requests: List<NetworkHcmcStatisticsDetailItem> = emptyList()
)

@Serializable
data class NetworkHcmcStatisticsDetailItem(
    val id: Int,
    val name: String? = null,
    val service_name: String? = null,
    val request_date: String? = null,
    val expired_date: String? = null,
    val final_state: String? = null,
    val request_user_name: String? = null,
    val processing_user_name: String? = null
)
