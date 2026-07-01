package com.bdsoftware.idorm.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bdsoftware.idorm.feature.auth.forgotpassword.ForgotPasswordRoute
import com.bdsoftware.idorm.feature.auth.login.LoginRoute
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object ForgotPasswordRoute

fun NavController.navigateToLogin() {
    navigate(LoginRoute)
}

fun NavController.navigateToForgotPassword() {
    navigate(ForgotPasswordRoute)
}

fun NavGraphBuilder.authGraph(
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    composable<LoginRoute> {
        LoginRoute(
            onNavigateToForgotPassword = onNavigateToForgotPassword,
            onLoginSuccess = onLoginSuccess,
            onChangeLanguage = onChangeLanguage
        )
    }
    
    composable<ForgotPasswordRoute> {
        ForgotPasswordRoute(
            onBackClick = onBackClick
        )
    }
}
