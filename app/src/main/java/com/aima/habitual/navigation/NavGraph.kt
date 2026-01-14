package com.aima.habitual.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aima.habitual.ui.screens.*

/**
 * SetupNavGraph defines the navigation structure of the app.
 * It maps Screen routes to their respective Composable screens.
 */
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // 1. Dashboard Screen (Main Entry)
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        // 2. WellBeing Screen
        composable(route = Screen.WellBeing.route) {
            WellBeingScreen()
        }

        // 3. Diary Screen
        composable(route = Screen.Diary.route) {
            DiaryScreen()
        }

        // 4. User Profile Screen
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }

        // 5. Habit Details Screen (Add/Edit Form)
        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") {
                type = NavType.StringType
                nullable = true
                defaultValue = "new"
            })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitDetailScreen(habitId = habitId)
        }

        // 6. Habit Statistics Screen (New Destination)
        composable(
            route = Screen.HabitStats.route,
            arguments = listOf(navArgument("habitId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            // Navigating to the Stats screen with the specific habit ID
            HabitStatsScreen(habitId = habitId, navController = navController)
        }
    }
}