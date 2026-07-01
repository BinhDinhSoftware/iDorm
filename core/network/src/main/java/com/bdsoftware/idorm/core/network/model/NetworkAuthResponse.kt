package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkAuthResponse(
    val Data: AuthData? = null,
    val Message: String? = null,
    val Success: Boolean = true
)

@Serializable
data class AuthData(
    val Token: String
)
