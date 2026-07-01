package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkInvoiceResponse(
    val Id: Int,
    val CreatedDate: String = "",
    val Total: Double = 0.0,
    val PaymentMethodList: String? = null,
    val IsPayment: Boolean = false,
    val Scholastic: String? = null,
    val DormitoryFullName: String? = null,
    val ListInvoiceDetail: List<NetworkInvoiceDetail> = emptyList()
)

@Serializable
data class NetworkInvoiceDetail(
    val Id: Int,
    val FeeName: String = "",
    val Amount: Double = 0.0,
    val Code: String? = null
)
