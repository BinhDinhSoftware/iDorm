package com.bdsoftware.idorm.feature.hcmc.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bdsoftware.idorm.feature.hcmc.HcmcScreen
import com.bdsoftware.idorm.feature.hcmc.HcmcNotificationScreen
import com.bdsoftware.idorm.feature.hcmc.HcmcCreateRequestScreen
import com.bdsoftware.idorm.feature.hcmc.HcmcRequestFormScreen
import com.bdsoftware.idorm.feature.hcmc.HcmcRequestDetailScreen
import com.bdsoftware.idorm.feature.hcmc.HcmcRequestEditScreen
import kotlinx.serialization.Serializable

@Serializable
object HcmcRoute

@Serializable
object HcmcNotificationsRoute

@Serializable
object HcmcCreateRequestRoute

@Serializable
data class HcmcRequestFormRoute(val serviceId: Int)

@Serializable
data class HcmcRequestDetailRoute(val requestId: Int)

@Serializable
data class HcmcRequestEditRoute(val requestId: Int, val serviceId: Int)

fun NavController.navigateToHcmc() {
    navigate(HcmcRoute)
}

fun NavController.navigateToHcmcNotifications() {
    navigate(HcmcNotificationsRoute)
}

fun NavController.navigateToHcmcCreateRequest() {
    navigate(HcmcCreateRequestRoute)
}

fun NavController.navigateToHcmcRequestForm(serviceId: Int) {
    navigate(HcmcRequestFormRoute(serviceId))
}

fun NavController.navigateToHcmcRequestDetail(requestId: Int) {
    navigate(HcmcRequestDetailRoute(requestId))
}

fun NavController.navigateToHcmcRequestEdit(requestId: Int, serviceId: Int) {
    navigate(HcmcRequestEditRoute(requestId, serviceId))
}

fun NavGraphBuilder.hcmcScreen(
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    composable<HcmcRoute> {
        HcmcScreen(
            onBack = onBack,
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToCreateRequest = onNavigateToCreateRequest,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}

fun NavGraphBuilder.hcmcNotificationScreen(
    onBack: () -> Unit
) {
    composable<HcmcNotificationsRoute> {
        HcmcNotificationScreen(
            onBack = onBack
        )
    }
}

fun NavGraphBuilder.hcmcCreateRequestScreen(
    onBack: () -> Unit,
    onNavigateToForm: (Int) -> Unit
) {
    composable<HcmcCreateRequestRoute> {
        HcmcCreateRequestScreen(
            onBack = onBack,
            onNavigateToForm = onNavigateToForm
        )
    }
}

fun NavGraphBuilder.hcmcRequestFormScreen(
    onBack: () -> Unit,
    onNavigateToMainHcmc: () -> Unit
) {
    composable<HcmcRequestFormRoute> {
        HcmcRequestFormScreen(
            onBack = onBack,
            onNavigateToMainHcmc = onNavigateToMainHcmc
        )
    }
}

fun NavGraphBuilder.hcmcRequestDetailScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (requestId: Int, serviceId: Int) -> Unit
) {
    composable<HcmcRequestDetailRoute> {
        HcmcRequestDetailScreen(
            onBack = onBack,
            onNavigateToEdit = onNavigateToEdit
        )
    }
}

fun NavGraphBuilder.hcmcRequestEditScreen(
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    composable<HcmcRequestEditRoute> {
        HcmcRequestEditScreen(
            onBack = onBack,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}
