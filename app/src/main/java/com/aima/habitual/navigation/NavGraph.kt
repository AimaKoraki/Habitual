package com.aima.habitual.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aima.habitual.ui.screens.*
import com.aima.habitual.ui.theme.AppTheme
import com.aima.habitual.viewmodel.CompanionViewModel
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * NavGraph manages the entire navigation structure.
 * Updated to handle Auth flow and Master/Detail logic.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: HabitViewModel,
    companionViewModel: CompanionViewModel,
    isDarkTheme: Boolean,
    appTheme: AppTheme,
    onThemeChange: (Boolean) -> Unit,
    onThemeColorChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val googleWebClientId = "19077224952-0im1f62fpj4i4sskbffqn26lco5u3tf7.apps.googleusercontent.com"

    NavHost(
        navController = navController,
        // Start at Login if not authenticated, otherwise Dashboard
        startDestination = if (viewModel.isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } }
    ) {

        // --- 1. AUTH FLOW ---

        composable(Screen.Login.route) {
            val context = LocalContext.current
            val activity = context as? FragmentActivity

            // Reactive navigation: when async Google sign-in sets isLoggedIn = true,
            // this effect triggers navigation to Dashboard.
            LaunchedEffect(viewModel.isLoggedIn) {
                if (viewModel.isLoggedIn) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                errorMessage = viewModel.loginError,
                onLoginAttempt = { email, password ->
                    viewModel.validateLogin(email, password)
                    // Navigation handled by LaunchedEffect above
                },
                onNavigateToRegister = {
                    viewModel.clearLoginError()
                    navController.navigate(Screen.Register.route)
                },
                onGoogleSignIn = {
                    viewModel.signInWithGoogle(context, googleWebClientId)
                    // Navigation handled by LaunchedEffect above
                },
                onBiometricLogin = {
                    activity?.let { act ->
                        viewModel.showBiometricPrompt(
                            activity = act,
                            onSuccess = {
                                // Navigation handled by LaunchedEffect above
                            },
                            onFailure = { errorMsg ->
                                // loginError is set by the callback if needed
                            }
                        )
                    }
                },
                isBiometricAvailable = viewModel.isBiometricAvailable
            )
        }

        composable(Screen.Register.route) {
            LaunchedEffect(viewModel.isLoggedIn) {
                if (viewModel.isLoggedIn) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            RegisterScreen(
                onRegisterSuccess = { name, email, password ->
                    // Save user to Firebase
                    viewModel.registerUser(name, email, password) { success ->
                        // Navigation handled by LaunchedEffect
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
                onAddClick = { isJournal ->
                    navController.navigate(Screen.DiaryDetail.createRoute("new", isJournal))
                }
            )
        }

        // --- 3. PROFILE & LOGOUT ---
        composable(Screen.Profile.route) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                appTheme = appTheme,
                onThemeChange = onThemeChange,
                onThemeColorChange = onThemeColorChange,
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        // Clears all history so user is fully logged out
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteProfile = {
                    viewModel.deleteProfile()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- 4. DETAILS & OTHER SCREENS ---

        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(
                navArgument("entryId") { defaultValue = "new" },
                navArgument("isJournal") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: "new"
            val isJournal = backStackEntry.arguments?.getBoolean("isJournal") ?: false
            DiaryDetailScreen(
                entryId = if (entryId == "new") null else entryId,
                isJournal = isJournal,
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

        // --- 4. COMPANIONS (Master/Detail from local sprite assets) ---
        composable(Screen.Companions.route) {
            CompanionsScreen(
                habitViewModel = viewModel,
                companionViewModel = companionViewModel,
                onCompanionClick = { companionName ->
                    navController.navigate(Screen.CompanionDetail.createRoute(companionName))
                }
            )
        }

        composable(
            route = Screen.CompanionDetail.route,
            arguments = listOf(navArgument("companionName") { type = NavType.StringType })
        ) { backStackEntry ->
            val companionName = backStackEntry.arguments?.getString("companionName") ?: ""
            CompanionDetailScreen(
                companionName = companionName,
                companionViewModel = companionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}