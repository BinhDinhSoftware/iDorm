package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkGenerateQrResponse(
    val success: Boolean = false,
    val bidvQr: NetworkQrCodeData? = null,
    val vcbQr: NetworkQrCodeData? = null,
    val message: String? = null
)

@Serializable
data class NetworkQrCodeData(
    val error: Int = 0,
    val base64: String? = null,
    val msg: String? = null
)
