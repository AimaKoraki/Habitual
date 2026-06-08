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
import com.aima.habitual.viewmodel.AuthViewModel
import com.aima.habitual.viewmodel.CompanionViewModel
import com.aima.habitual.viewmodel.DiaryViewModel
import com.aima.habitual.viewmodel.HabitViewModel
import com.aima.habitual.viewmodel.SettingsViewModel
import com.aima.habitual.viewmodel.WellbeingViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    habitViewModel: HabitViewModel,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    wellbeingViewModel: WellbeingViewModel,
    diaryViewModel: DiaryViewModel,
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
        startDestination = if (authViewModel.isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } }
    ) {

        composable(Screen.Login.route) {
            val context = LocalContext.current
            val activity = context as? FragmentActivity

            LaunchedEffect(authViewModel.isLoggedIn) {
                if (authViewModel.isLoggedIn) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                errorMessage = authViewModel.loginError,
                onLoginAttempt = { email, password ->
                    authViewModel.validateLogin(email, password)
                },
                onNavigateToRegister = {
                    authViewModel.clearLoginError()
                    navController.navigate(Screen.Register.route)
                },
                onGoogleSignIn = {
                    authViewModel.signInWithGoogle(context, googleWebClientId)
                },
                onBiometricLogin = {
                    activity?.let { act ->
                        authViewModel.showBiometricPrompt(
                            activity = act,
                            onSuccess = {
                            },
                            onFailure = { errorMsg ->
                            }
                        )
                    }
                },
                isBiometricAvailable = authViewModel.isBiometricAvailable
            )
        }

        composable(Screen.Register.route) {
            LaunchedEffect(authViewModel.isLoggedIn) {
                if (authViewModel.isLoggedIn) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            RegisterScreen(
                onRegisterSuccess = { name, email, password ->
                    authViewModel.registerUser(name, email, password) { success ->
                    }
                },
                onNavigateToLogin = {
                    authViewModel.clearLoginError()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = habitViewModel, authViewModel = authViewModel)
        }

        composable(Screen.WellBeing.route) {
            WellBeingScreen(navController = navController, viewModel = wellbeingViewModel, settingsViewModel = settingsViewModel)
        }

        composable(Screen.Diary.route) {
            DiaryScreen(
                navController = navController,
                viewModel = diaryViewModel,
                authViewModel = authViewModel,
                onEntryClick = { entryId ->
                    navController.navigate(Screen.DiaryView.createRoute(entryId))
                },
                onAddClick = { isJournal ->
                    navController.navigate(Screen.DiaryDetail.createRoute("new", isJournal))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                appTheme = appTheme,
                onThemeChange = onThemeChange,
                onThemeColorChange = onThemeColorChange,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                habitViewModel = habitViewModel,
                wellbeingViewModel = wellbeingViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteProfile = {
                    authViewModel.deleteProfile()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

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
                viewModel = diaryViewModel
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
                viewModel = habitViewModel
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
                 viewModel = habitViewModel
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
                 viewModel = diaryViewModel
            )
        }

        composable(Screen.Companions.route) {
            CompanionsScreen(
                habitViewModel = habitViewModel,
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