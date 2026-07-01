package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkRentItem(
    val Id: Int,
    val DormitoryFullName: String? = null,
    val DormitoryCode: String? = null,
    val CheckInDate: String? = null,
    val CheckOutDate: String? = null,
    val IsContinue: Boolean = false,
    val Status: String? = null,
    val Note: String? = null,
    val Scholastic: String? = null,
    val Semester: String? = null,
    val CreatedDate: String? = null,
    val DormitoryRoomTypeName: String? = null,
    val StudentId: Int? = null,
    val IsPayment: Boolean = false
)
