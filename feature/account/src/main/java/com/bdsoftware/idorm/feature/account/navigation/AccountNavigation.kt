package com.bdsoftware.idorm.feature.account.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object AccountRoute

@Serializable
object AccountSettingsRoute

fun NavController.navigateToAccount(navOptions: NavOptions? = null) {
    navigate(AccountRoute, navOptions)
}

fun NavController.navigateToAccountSettings(navOptions: NavOptions? = null) {
    navigate(AccountSettingsRoute, navOptions)
}
