package com.aima.habitual.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aima.habitual.ui.screens.*
import com.aima.habitual.viewmodel.HabitViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    // 1. Initialize the shared ViewModel
    val habitViewModel: HabitViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // 2. Dashboard Screen: Now receives the viewModel
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = habitViewModel)
        }

        composable(route = Screen.WellBeing.route) { WellBeingScreen() }
        composable(route = Screen.Diary.route) { DiaryScreen() }

        composable(route = Screen.Profile.route) {
            ProfileScreen(isDarkTheme = isDarkTheme, onThemeChange = onThemeChange)
        }

        // 3. Habit Detail Screen: Form now saves to the same viewModel
        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") {
                type = NavType.StringType
                nullable = true
                defaultValue = "new"
            })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitDetailScreen(
                habitId = habitId,
                navController = navController,
                viewModel = habitViewModel // Shared instance
            )
        }

        // 4. Habit Stats Screen: Uses records from the viewModel
        composable(
            route = Screen.HabitStats.route,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitStatsScreen(
                habitId = habitId,
                navController = navController,
                viewModel = habitViewModel // Shared instance
            )
        }
    }
}