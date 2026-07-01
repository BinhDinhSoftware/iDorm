package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkLoginRequest(
    val StudentCode: String,
    val PIN: String
)
