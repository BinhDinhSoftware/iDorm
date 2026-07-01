package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcBaseResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
