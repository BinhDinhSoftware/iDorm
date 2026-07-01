package com.bdsoftware.idorm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * App state holder theo kiến trúc Now in Android.
 * Quản lý trạng thái navigation tập trung, bao gồm:
 * - currentDestination: destination đang hiển thị
 * - topLevelDestinations: danh sách các tab chính
 * - shouldShowBottomBar: có hiển thị BottomBar hay không
 */
@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController()
): AppState {
    return remember(navController) {
        AppState(navController = navController)
    }
}

@Stable
class AppState(
    val navController: NavHostController
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries.toList()

    /**
     * Current navigation destination.
     * Cần gọi trong @Composable context.
     */
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    /**
     * BottomBar chỉ hiển thị khi currentDestination thuộc TopLevelDestination.
     * Đây là cách Now in Android quyết định hiển thị BottomBar.
     */
    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination?.isTopLevelDestination() == true

    private fun NavDestination.isTopLevelDestination(): Boolean {
        return hierarchy.any { destination ->
            val routeStr = destination.route ?: ""
            topLevelDestinations.any { topLevel ->
                val qualifiedName = topLevel.routeClass.qualifiedName ?: ""
                val simpleName = topLevel.routeClass.simpleName ?: ""
                routeStr == qualifiedName || routeStr.endsWith(simpleName)
            }
        }
    }
}
