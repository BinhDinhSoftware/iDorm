package com.bdsoftware.idorm.core.model

/**
 * Cấu hình cho một mạng WiFi Awing tại KTX.
 * Mỗi mạng mesh có thể có gateway/awingUrl khác nhau.
 */
data class WifiNetworkConfig(
    val ssid: String,
    val gatewayUrl: String = "http://186.186.0.1",
    val awingUrl: String = "http://v1.awingconnect.vn",
    val enabled: Boolean = true
)
