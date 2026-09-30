package com.example.viora.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.viora.ui.dashboard.DashboardScreen
import com.example.viora.ui.focus.FocusActiveScreen
import com.example.viora.ui.focus.FocusHubScreen
import com.example.viora.ui.insights.InsightsScreen
import com.example.viora.ui.apps.MonitoredAppsScreen
import com.example.viora.ui.onboarding.OnboardingScreen
import com.example.viora.ui.settings.SettingsScreen
import com.example.viora.mindful.ui.MindfulUnlockSettingsScreen
import com.example.viora.mindful.ui.MindfulUnlockStatsScreen

@Composable
fun VioraNavHost(
    navController: NavHostController,
    onOnboardingComplete: (dailyGoalMinutes: Int) -> Unit = { _ -> },
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
            )
        },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = { dailyGoalMinutes ->
                    onOnboardingComplete(dailyGoalMinutes)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToFocus = { navController.navigate(Screen.FocusHub.route) },
                onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.FocusHub.route) {
            FocusHubScreen(
                onStartFocus = { type, duration ->
                    navController.navigate(Screen.FocusActive.createRoute(type, duration))
                }
            )
        }

        composable(
            route = Screen.FocusActive.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "DEEP_WORK"
            val duration = backStackEntry.arguments?.getInt("duration") ?: 25
            FocusActiveScreen(
                focusType = type,
                durationMinutes = duration,
                onFinished = { navController.popBackStack() }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen()
        }

        composable(Screen.Apps.route) {
            MonitoredAppsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToMindfulUnlock = { navController.navigate(Screen.MindfulUnlockSettings.route) }
            )
        }

        composable(Screen.MindfulUnlockSettings.route) {
            MindfulUnlockSettingsScreen()
        }

        composable(Screen.MindfulUnlockStats.route) {
            MindfulUnlockStatsScreen()
        }
    }
}
