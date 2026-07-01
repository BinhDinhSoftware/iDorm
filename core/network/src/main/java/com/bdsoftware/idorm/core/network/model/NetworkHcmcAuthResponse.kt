package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcAuthResponse(
    val success: Boolean,
    val message: String? = null,
    val data: NetworkHcmcAuthData? = null
)

@Serializable
data class NetworkHcmcAuthData(
    val id: Int,
    val fullname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val gender: Boolean? = null,
    val birthday: String? = null,
    val student_code: String? = null,
    val avatar_url: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null
)
