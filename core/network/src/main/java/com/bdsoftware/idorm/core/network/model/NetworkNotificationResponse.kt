package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkNotificationResponse(
    val ListNotificationSystem: List<NotificationData>? = null,
    val ListNotificationAdmin: List<NotificationData>? = null,
)

@Serializable
data class NotificationData(
    val Id: Int,
    val Titles: String = "",
    val Content: String = "",
    val CreatedDate: String = "",
    val ModifiedDate: String = "",
    val IsRead: Boolean = false,
    val Type: String = "",
    val Image: String? = null,
    val IsDeleted: Boolean? = null,
    val TargetId: Int = 0,
    val TargetModule: String = "",
    val DormitoryAreaId: Int? = null,
    val DormitoryHouseId: Int? = null,
    val DormitoryRoomId: Int? = null,
    val NotificationStudentId: Int? = null,
    val NotificationStudentIsDeleted: Boolean? = null,
    val CreatedUserId: String? = null,
    val CreatedUserName: String? = null,
    val ModifiedUserId: String? = null,
    val ModifiedUserName: String? = null,
)
