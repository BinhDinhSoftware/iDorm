package com.bdsoftware.idorm.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector
import com.bdsoftware.idorm.core.designsystem.R as DesignR
import com.bdsoftware.idorm.feature.home.navigation.HomeRoute
import com.bdsoftware.idorm.feature.invoice.navigation.InvoiceRoute
import com.bdsoftware.idorm.feature.notification.navigation.NotificationRoute
import com.bdsoftware.idorm.feature.account.navigation.AccountRoute
import kotlin.reflect.KClass

/**
 * Top-level destinations theo kiến trúc Now in Android.
 * Chỉ các destination này mới hiển thị BottomBar.
 */
enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val labelResId: Int,
    val routeClass: KClass<*>
) {
    HOME(
        route = HomeRoute,
        icon = Icons.Default.Home,
        labelResId = DesignR.string.tab_home,
        routeClass = HomeRoute::class
    ),
    INVOICE(
        route = InvoiceRoute,
        icon = Icons.Default.Receipt,
        labelResId = DesignR.string.tab_invoice,
        routeClass = InvoiceRoute::class
    ),
    NOTIFICATION(
        route = NotificationRoute,
        icon = Icons.Default.Notifications,
        labelResId = DesignR.string.tab_notification,
        routeClass = NotificationRoute::class
    ),
    ACCOUNT(
        route = AccountRoute,
        icon = Icons.Default.Person,
        labelResId = DesignR.string.tab_account,
        routeClass = AccountRoute::class
    )
}
