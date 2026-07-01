package com.bdsoftware.idorm.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.data.repository.WifiWorkerManager
import com.bdsoftware.idorm.feature.auth.navigation.LoginRoute
import com.bdsoftware.idorm.feature.auth.navigation.authGraph
import com.bdsoftware.idorm.feature.auth.navigation.navigateToForgotPassword
import com.bdsoftware.idorm.feature.home.HomeScreen
import com.bdsoftware.idorm.feature.home.navigation.HomeRoute
import com.bdsoftware.idorm.feature.profile.navigation.ProfileRoute
import com.bdsoftware.idorm.feature.account.navigation.AccountSettingsRoute
import com.bdsoftware.idorm.feature.rent.navigation.RentHistoryRoute
import com.bdsoftware.idorm.feature.wificonfig.navigation.WifiConfigRoute
import com.bdsoftware.idorm.feature.account.AccountSettingsScreen
import com.bdsoftware.idorm.feature.rent.RentHistoryScreen
import com.bdsoftware.idorm.feature.wificonfig.WifiConfigScreen
import com.bdsoftware.idorm.feature.profile.ProfileScreen
import com.bdsoftware.idorm.feature.feedback.navigation.feedbackScreen
import com.bdsoftware.idorm.feature.feedback.navigation.navigateToFeedback
import com.bdsoftware.idorm.feature.hcmc.navigation.hcmcScreen
import com.bdsoftware.idorm.feature.hcmc.navigation.hcmcNotificationScreen
import com.bdsoftware.idorm.feature.hcmc.navigation.hcmcCreateRequestScreen
import com.bdsoftware.idorm.feature.hcmc.navigation.hcmcRequestFormScreen
import com.bdsoftware.idorm.feature.hcmc.navigation.hcmcRequestDetailScreen
import com.bdsoftware.idorm.feature.hcmc.navigation.hcmcRequestEditScreen
import com.bdsoftware.idorm.feature.hcmc.navigation.navigateToHcmc
import com.bdsoftware.idorm.feature.hcmc.navigation.navigateToHcmcNotifications
import com.bdsoftware.idorm.feature.hcmc.navigation.navigateToHcmcCreateRequest
import com.bdsoftware.idorm.feature.hcmc.navigation.navigateToHcmcRequestForm
import com.bdsoftware.idorm.feature.hcmc.navigation.navigateToHcmcRequestDetail
import com.bdsoftware.idorm.feature.hcmc.navigation.navigateToHcmcRequestEdit
import com.bdsoftware.idorm.feature.hcmc.navigation.HcmcRoute
import com.bdsoftware.idorm.feature.payment.navigation.paymentScreen
import com.bdsoftware.idorm.feature.payment.navigation.navigateToPayment
import com.bdsoftware.idorm.feature.invoice.InvoiceScreen
import com.bdsoftware.idorm.feature.invoice.navigation.InvoiceRoute
import com.bdsoftware.idorm.feature.notification.NotificationScreen
import com.bdsoftware.idorm.feature.notification.navigation.NotificationRoute
import com.bdsoftware.idorm.feature.account.AccountScreen
import com.bdsoftware.idorm.feature.account.navigation.AccountRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IDormTabScaffold(
    appState: AppState,
    unreadNotificationCount: Int,
    unpaidInvoiceCount: Int,
    content: @Composable (PaddingValues) -> Unit
) {
    val navController = appState.navController
    val currentDestination = appState.currentDestination

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFFE0E0E0)
                )
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = Color.Gray
                ) {
                    appState.topLevelDestinations.forEach { destination ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.routeClass)
                        } == true

                        NavigationBarItem(
                            icon = {
                                val badgeCount = when (destination) {
                                    TopLevelDestination.NOTIFICATION -> unreadNotificationCount
                                    TopLevelDestination.INVOICE -> unpaidInvoiceCount
                                    else -> 0
                                }
                                if (badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text(text = badgeCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = destination.icon, contentDescription = null)
                                    }
                                } else {
                                    Icon(imageVector = destination.icon, contentDescription = null)
                                }
                            },
                            label = { Text(text = stringResource(destination.labelResId)) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0073C0),
                                selectedTextColor = Color(0xFF0073C0),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFE3F2FD)
                            ),
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
fun AppNavHost(
    appState: AppState,
    isLoggedIn: Boolean,
    preferencesDataSource: IDormPreferencesDataSource,
    wifiWorkerManager: WifiWorkerManager,
    unreadNotificationCount: Int,
    unpaidInvoiceCount: Int,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    val startDestination: Any = if (isLoggedIn) HomeRoute else LoginRoute
    val coroutineScope = rememberCoroutineScope()
    val onChangeLanguage: (String) -> Unit = { lang ->
        coroutineScope.launch {
            preferencesDataSource.saveAppLanguage(lang)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        modifier = modifier
    ) {
        authGraph(
            onNavigateToForgotPassword = { navController.navigateToForgotPassword() },
            onLoginSuccess = {
                navController.navigate(HomeRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            },
            onBackClick = { navController.popBackStack() },
            onChangeLanguage = onChangeLanguage
        )

        composable<HomeRoute> {
            IDormTabScaffold(
                appState = appState,
                unreadNotificationCount = 0,
                unpaidInvoiceCount = unpaidInvoiceCount
            ) { paddingValues ->
                HomeScreen(
                    onViewAllNotifications = {
                        navController.navigate(NotificationRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToHcmc = {
                        navController.navigateToHcmc()
                    },
                    onNavigateToRentHistory = {
                        navController.navigate(RentHistoryRoute)
                    },
                    onNavigateToFeedback = {
                        navController.navigateToFeedback()
                    },
                    onNavigateToWifiConfig = {
                        navController.navigate(WifiConfigRoute)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                )
            }
        }

        composable<InvoiceRoute> {
            IDormTabScaffold(
                appState = appState,
                unreadNotificationCount = 0,
                unpaidInvoiceCount = unpaidInvoiceCount
            ) { paddingValues ->
                InvoiceScreen(
                    onNavigateToPayment = { invoiceId, amount, isEw ->
                        navController.navigateToPayment(invoiceId, amount, isEw)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                )
            }
        }

        composable<NotificationRoute> {
            IDormTabScaffold(
                appState = appState,
                unreadNotificationCount = 0,
                unpaidInvoiceCount = unpaidInvoiceCount
            ) { paddingValues ->
                NotificationScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                )
            }
        }

        composable<AccountRoute> {
            IDormTabScaffold(
                appState = appState,
                unreadNotificationCount = 0,
                unpaidInvoiceCount = unpaidInvoiceCount
            ) { paddingValues ->
                AccountScreen(
                    onLogout = {
                        coroutineScope.launch {
                            wifiWorkerManager.stopAutoRenewWorker()
                            preferencesDataSource.clearToken()
                        }
                        navController.navigate(LoginRoute) {
                            popUpTo(HomeRoute) { inclusive = true }
                        }
                    },
                    onChangeLanguage = onChangeLanguage,
                    onNavigateToProfile = {
                        navController.navigate(ProfileRoute)
                    },
                    onNavigateToAccountSettings = {
                        navController.navigate(AccountSettingsRoute)
                    },
                    onNavigateToFeedback = {
                        navController.navigateToFeedback()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                )
            }
        }

        composable<WifiConfigRoute> {
            WifiConfigScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<RentHistoryRoute> {
            RentHistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<AccountSettingsRoute> {
            AccountSettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }

        feedbackScreen(
            onBack = {
                navController.popBackStack()
            },
            onHomeClick = {
                navController.navigate(HomeRoute) {
                    popUpTo(HomeRoute) { inclusive = true }
                }
            }
        )

        hcmcScreen(
            onBack = {
                navController.popBackStack()
            },
            onNavigateToNotifications = {
                navController.navigateToHcmcNotifications()
            },
            onNavigateToCreateRequest = {
                navController.navigateToHcmcCreateRequest()
            },
            onNavigateToDetail = { requestId ->
                navController.navigateToHcmcRequestDetail(requestId)
            }
        )

        hcmcNotificationScreen(
            onBack = {
                navController.popBackStack()
            }
        )

        hcmcCreateRequestScreen(
            onBack = {
                navController.popBackStack()
            },
            onNavigateToForm = { serviceId ->
                navController.navigateToHcmcRequestForm(serviceId)
            }
        )

        hcmcRequestFormScreen(
            onBack = {
                navController.popBackStack()
            },
            onNavigateToMainHcmc = {
                navController.popBackStack(HcmcRoute, false)
            }
        )

        hcmcRequestDetailScreen(
            onBack = {
                navController.popBackStack()
            },
            onNavigateToEdit = { requestId, serviceId ->
                navController.navigateToHcmcRequestEdit(requestId, serviceId)
            }
        )

        hcmcRequestEditScreen(
            onBack = {
                navController.popBackStack()
            },
            onNavigateToDetail = {
                navController.popBackStack()
            }
        )

        paymentScreen(
            onBack = {
                navController.popBackStack()
            },
            onPaymentSuccess = {
                navController.popBackStack()
            }
        )
    }
}
