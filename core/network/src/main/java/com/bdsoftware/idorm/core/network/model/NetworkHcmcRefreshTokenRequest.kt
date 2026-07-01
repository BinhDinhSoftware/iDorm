package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcRefreshTokenRequest(
    val refresh_token: String
)
