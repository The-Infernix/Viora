package com.example.viora.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object FocusHub : Screen("focus")
    data object FocusActive : Screen("focus/active/{type}/{duration}") {
        fun createRoute(type: String, duration: Int) = "focus/active/$type/$duration"
    }
    data object Insights : Screen("insights")
    data object Apps : Screen("apps")
    data object AppDetail : Screen("apps/{packageName}") {
        fun createRoute(packageName: String) = "apps/$packageName"
    }
    data object Settings : Screen("settings")
    data object MindfulUnlockSettings : Screen("settings/mindful-unlock")
    data object MindfulUnlockStats : Screen("settings/mindful-unlock/stats")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.FocusHub, "Focus", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
    BottomNavItem(Screen.Insights, "Insights", Icons.Outlined.Analytics, Icons.Outlined.Analytics),
    BottomNavItem(Screen.Apps, "Apps", Icons.Outlined.Apps, Icons.Outlined.Apps),
)
