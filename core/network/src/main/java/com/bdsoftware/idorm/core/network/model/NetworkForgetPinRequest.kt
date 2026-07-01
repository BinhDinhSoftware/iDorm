package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkForgetPinRequest(
    val Email: String,
    val StudentCode: String
)
