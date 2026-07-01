package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcServiceGroup(
    val id: Int,
    val name: String,
    val description: String? = null,
    val services: List<NetworkHcmcService> = emptyList()
)

@Serializable
data class NetworkHcmcService(
    val id: Int,
    val name: String,
    val description: String? = null,
    val titlenote: String? = null,
    val state: String? = null,
    val duration: Int? = null,
    val files: List<String> = emptyList()
)
