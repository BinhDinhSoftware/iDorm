package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable
import com.bdsoftware.idorm.core.network.serializer.FlexibleStringSerializer

@Serializable
data class NetworkHcmcRequestDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: NetworkHcmcRequestDetail? = null
)

@Serializable
data class NetworkHcmcRequestDetail(
    val id: Int,
    val service_id: Int? = null,
    val service_name: String? = null,
    val service_description: String? = null,
    val name: String? = null,
    val note: String? = null,
    val inputs: List<NetworkHcmcRequestInput>? = emptyList(),
    val attachments: List<Int>? = emptyList(),
    val image_attachment_ids: List<NetworkHcmcAttachment>? = emptyList(),
    val request_date: String? = null,
    val expired_date: String? = null,
    val is_overdue: Boolean? = false,
    val is_warning: Boolean? = false,
    val request_user_id: Int? = null,
    val request_user_name: String? = null,
    val request_user_phone: String? = null,
    val request_user_dormitory_full: String? = null,
    val request_user_dormitory_house: String? = null,
    val request_user_dormitory_room: String? = null,
    val user_processing_id: Int? = null,
    val user_processing_name: String? = null,
    val cancel_reason: String? = null,
    val cancel_date: String? = null,
    val final_state: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val approve_content: String? = null,
    val approve_date: String? = null,
    val approve_user_id: Int? = null,
    val approve_user_name: String? = null,
    val histories: List<NetworkHcmcHistoryItem>? = emptyList(),
    val step_ids: List<NetworkHcmcStepItem>? = emptyList()
)

@Serializable
data class NetworkHcmcRequestInput(
    val name: String,
    val label: String,
    val type: String,
    val value_display: String? = null,
    val required: Boolean = false
)

@Serializable
data class NetworkHcmcAttachment(
    val id: Int,
    val name: String,
    val url: String
)

@Serializable
data class NetworkHcmcHistoryItem(
    val id: Int,
    val step_id: Int? = null,
    val step_name: String? = null,
    val state: String? = null,
    val user_id: Int? = null,
    val user_name: String? = null,
    val note: String? = null,
    val date: String? = null
)

@Serializable
data class NetworkHcmcStepHistoryItem(
    val id: Int,
    val state: String? = null,
    val user_id: Int? = null,
    val user_name: String? = null,
    val note: String? = null,
    val date: String? = null
)

@Serializable
data class NetworkHcmcStepItem(
    val id: Int,
    val sequence: Int? = null,
    val step_id: Int? = null,
    val step_name: String? = null,
    val step_description: String? = null,
    val name: String? = null,
    val state: String? = null,
    val activated: Boolean? = null,
    val disabled: Boolean? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val approve_content: String? = null,
    val approve_date: String? = null,
    val assign_user_id: Int? = null,
    val assign_user_name: String? = null,
    val assigned_department_id: Int? = null,
    val assigned_department_name: String? = null,
    val history_ids: List<NetworkHcmcStepHistoryItem>? = emptyList()
)
