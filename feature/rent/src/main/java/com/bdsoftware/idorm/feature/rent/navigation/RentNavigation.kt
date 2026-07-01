package com.bdsoftware.idorm.feature.rent.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object RentHistoryRoute

fun NavController.navigateToRentHistory(navOptions: NavOptions? = null) {
    navigate(RentHistoryRoute, navOptions)
}
