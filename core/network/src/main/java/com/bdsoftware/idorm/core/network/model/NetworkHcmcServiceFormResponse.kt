package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkHcmcServiceFormResponse(
    val service_id: Int,
    val service_name: String,
    val fields: List<NetworkHcmcFormField> = emptyList()
)

@Serializable
data class NetworkHcmcFormField(
    val id: Int,
    val name: String,
    val label: String,
    val type: String,
    val required: Boolean = false,
    val placeholder: String? = null,
    val sequence: Int = 10,
    val options: List<NetworkHcmcFormFieldOption> = emptyList()
)

@Serializable
data class NetworkHcmcFormFieldOption(
    val id: Int,
    val name: String
)
