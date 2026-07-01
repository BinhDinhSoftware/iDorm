package com.bdsoftware.idorm.feature.payment.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bdsoftware.idorm.feature.payment.PaymentScreen
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRoute(
    val invoiceId: Int,
    val amount: Double,
    val isEw: Boolean
)

fun NavController.navigateToPayment(
    invoiceId: Int,
    amount: Double,
    isEw: Boolean,
    navOptions: NavOptions? = null
) {
    navigate(
        PaymentRoute(
            invoiceId = invoiceId,
            amount = amount,
            isEw = isEw
        ),
        navOptions
    )
}

fun NavGraphBuilder.paymentScreen(
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    composable<PaymentRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<PaymentRoute>()
        PaymentScreen(
            invoiceId = route.invoiceId,
            amount = route.amount,
            isEw = route.isEw,
            onBack = onBack,
            onPaymentSuccess = onPaymentSuccess
        )
    }
}
