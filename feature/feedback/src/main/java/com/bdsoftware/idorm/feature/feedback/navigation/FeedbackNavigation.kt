package com.bdsoftware.idorm.feature.feedback.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.bdsoftware.idorm.feature.feedback.FeedbackRoute
import kotlinx.serialization.Serializable

@Serializable
object FeedbackRoutePattern

fun NavController.navigateToFeedback(navOptions: NavOptions? = null) {
    navigate(FeedbackRoutePattern, navOptions)
}

fun NavGraphBuilder.feedbackScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit
) {
    composable<FeedbackRoutePattern> {
        FeedbackRoute(
            onBack = onBack,
            onHomeClick = onHomeClick
        )
    }
}
