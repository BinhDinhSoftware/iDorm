package com.bdsoftware.idorm.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkInvoiceEWResponse(
    val Id: Int,
    val CreatedDate: String = "",
    val Year: Int = 0,
    val Month: Int = 0,
    val CollectedDate: String? = null,
    val EFirstIndex: Double? = null,
    val ELastIndex: Double? = null,
    val WFirstIndex: Double? = null,
    val WLastIndex: Double? = null,
    val ETotal: Double? = null,
    val WTotal: Double? = null,
    val IsPayment: Boolean = false,
    val TotalStudent: Int = 0,
    val ListInvoiceEWSubDetail: List<NetworkInvoiceEWSubDetail> = emptyList()
)

@Serializable
data class NetworkInvoiceEWSubDetail(
    val Id: Int,
    val Type: Boolean = false,
    val Quantity: Double = 0.0,
    val Price: Double = 0.0,
    val Amount: Double = 0.0
)
