package com.bdsoftware.idorm.feature.invoice.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object InvoiceRoute

fun NavController.navigateToInvoice(navOptions: NavOptions? = null) {
    navigate(InvoiceRoute, navOptions)
}
