package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkChangePinRequest(
    val Email: String,
    val OldPIN: String,
    val NewPIN: String,
    val ConfirmPIN: String
)
