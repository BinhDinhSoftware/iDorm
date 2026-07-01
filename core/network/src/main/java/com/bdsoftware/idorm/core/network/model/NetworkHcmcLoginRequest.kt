package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcLoginRequest(
    val username: String,
    val password: String,
    val fcm_device_token: String,
    val device_id: String
)
