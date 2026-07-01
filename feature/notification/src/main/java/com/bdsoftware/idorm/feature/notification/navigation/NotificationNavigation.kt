package com.bdsoftware.idorm.feature.notification.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object NotificationRoute

fun NavController.navigateToNotification(navOptions: NavOptions? = null) {
    navigate(NotificationRoute, navOptions)
}
