package com.bdsoftware.idorm.feature.wificonfig.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object WifiConfigRoute

fun NavController.navigateToWifiConfig(navOptions: NavOptions? = null) {
    navigate(WifiConfigRoute, navOptions)
}
