package com.bdsoftware.idorm.core.model

data class PaymentDetails(
    val amount: Double = 0.0,
    val bidvQrBase64: String = "",
    val vcbQrBase64: String = ""
)
