package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkChangePinResponse(
    val Status: Boolean = true,
    val Message: String? = null
)
