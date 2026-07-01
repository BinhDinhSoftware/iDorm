package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkUpdateNotificationRequest(
    val NotificationId: String,
    val NotificationStudentId: Int? = null
)
