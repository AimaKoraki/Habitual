package com.aima.habitual.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.BottomNavigationBar
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * MainScreen: The root container of the app.
 * Handles Navigation, Responsive Layouts, and Authentication State Logic.
 */
@Composable
fun MainScreen(
    windowSizeClass: WindowWidthSizeClass,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    // 1. Core State
    val navController = rememberNavController()
    val viewModel: HabitViewModel = viewModel() // Shared ViewModel
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 2. Responsive Logic
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Show Side Rail if screen is Wide (Tablet) OR Landscape (Phone rotated)
    val showSideRail = windowSizeClass != WindowWidthSizeClass.Compact || isLandscape

    // Navigation item definitions
    val mainTabs = listOf(Screen.Dashboard, Screen.WellBeing, Screen.Diary, Screen.Profile)
    val showBars = currentRoute in mainTabs.map { it.route }

    Scaffold(
        bottomBar = {
            // ONLY show Bottom Bar on Portrait Phones if logged in and on a main tab
            if (!showSideRail && showBars && viewModel.isLoggedIn) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // A. SIDE NAVIGATION RAIL (Landscape / Tablet)
            if (showSideRail && showBars && viewModel.isLoggedIn) {
                NavigationRail {
                    mainTabs.forEach { screen ->
                        NavigationRailItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.titleRes)) }
                        )
                    }
                }
            }

            // B. NAVIGATION HOST
            NavHost(
                navController = navController,
                // Automatically skips login if the user session is still active
                startDestination = if (viewModel.isLoggedIn) Screen.Dashboard.route else "login",
                modifier = Modifier.weight(1f)
            ) {

                // --- AUTHENTICATION FLOW ---

                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            viewModel.login()
                            navController.navigate(Screen.Dashboard.route) {
                                // FIXED: Clears "login" from backstack so Back button exits app
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate("register")
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = { nameFromForm -> // Receive the name here
                            viewModel.updateUserName(nameFromForm) // Save it to the ViewModel/Prefs
                            viewModel.login() // Set login state
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // --- MAIN APP TABS ---

                composable(Screen.Dashboard.route) {
                    DashboardScreen(navController, viewModel)
                }

                composable(Screen.WellBeing.route) {
                    WellBeingScreen(navController, viewModel)
                }

                // --- MASTER / DETAIL FLOW (DIARY) ---

                composable(Screen.Diary.route) {
                    DiaryScreen(
                        navController = navController,
                        viewModel = viewModel,
                        onEntryClick = { entryId: String ->
                            if (entryId == "new") {
                                navController.navigate("diary_detail/new")
                            } else {
                                navController.navigate("diary_detail/$entryId")
                            }
                        }
                    )
                }

                composable("diary_detail/{entryId}") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId")
                    DiaryDetailScreen(
                        entryId = if (entryId == "new") null else entryId,
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                // --- PROFILE & LOGOUT ---

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange,
                        viewModel = viewModel,
                        // This is the missing parameter!
                        onLogout = {
                            viewModel.logout() // Clear the "isLoggedIn" state in the ViewModel
                            navController.navigate("login") {
                                // Clear the navigation history so the user can't "back" into the app
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}