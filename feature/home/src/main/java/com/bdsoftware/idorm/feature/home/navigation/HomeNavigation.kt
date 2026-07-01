package com.bdsoftware.idorm.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bdsoftware.idorm.feature.home.HomeScreen
import kotlinx.serialization.Serializable

import androidx.compose.ui.Modifier

@Serializable
object HomeRoute

fun NavGraphBuilder.homeGraph(
    onViewAllNotifications: () -> Unit,
    onNavigateToHcmc: () -> Unit,
    onNavigateToRentHistory: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToWifiConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    composable<HomeRoute> {
        HomeScreen(
            onViewAllNotifications = onViewAllNotifications,
            onNavigateToHcmc = onNavigateToHcmc,
            onNavigateToRentHistory = onNavigateToRentHistory,
            onNavigateToFeedback = onNavigateToFeedback,
            onNavigateToWifiConfig = onNavigateToWifiConfig,
            modifier = modifier
        )
    }
}
