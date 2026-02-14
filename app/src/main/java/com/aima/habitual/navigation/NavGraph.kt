package com.aima.habitual.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aima.habitual.ui.screens.*
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * NavGraph manages the entire navigation structure.
 * Updated to handle Auth flow and Master/Detail logic.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: HabitViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        // Start at Login if not authenticated, otherwise Dashboard
        startDestination = if (viewModel.isLoggedIn) Screen.Dashboard.route else "login",
        modifier = modifier,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } }
    ) {

        // --- 1. AUTH FLOW ---

        composable("login") {
            LoginScreen(
                errorMessage = viewModel.loginError, // Pass the error state
                onLoginAttempt = { email, password ->
                    // Validate using ViewModel
                    val isSuccess = viewModel.validateLogin(email, password)
                    if (isSuccess) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    viewModel.clearLoginError() // Clear errors when switching screens
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { name, email, password ->
                    // Save user to SharedPreferences
                    viewModel.registerUser(name, email, password)

                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    viewModel.clearLoginError()
                    navController.popBackStack()
                }
            )
        }

        // --- 2. MAIN APP TABS ---

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        composable(Screen.WellBeing.route) {
            WellBeingScreen(navController = navController, viewModel = viewModel)
        }

        composable(Screen.Diary.route) {
            DiaryScreen(
                navController = navController,
                viewModel = viewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Screen.DiaryView.createRoute(entryId))
                },
                onAddClick = {
                    navController.navigate(Screen.DiaryDetail.createRoute("new"))
                }
            )
        }

        // --- 3. PROFILE & LOGOUT ---
        composable(Screen.Profile.route) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") {
                        // Clears all history so user is fully logged out
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteProfile = {
                    viewModel.deleteProfile()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- 4. DETAILS & OTHER SCREENS ---

        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(navArgument("entryId") { defaultValue = "new" })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: "new"
            DiaryDetailScreen(
                entryId = if (entryId == "new") null else entryId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { defaultValue = "new" })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitDetailScreen(
                habitId = habitId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.HabitStats.route,
            arguments = listOf(navArgument("habitId") { defaultValue = "" })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitStatsScreen(
                 habitId = habitId,
                 navController = navController,
                 viewModel = viewModel
            )
        }
        composable(
            route = Screen.DiaryView.route,
            arguments = listOf(navArgument("entryId") { defaultValue = "" })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            DiaryViewScreen(
                 entryId = entryId,
                 navController = navController,
                 viewModel = viewModel
            )
        }
    }
}