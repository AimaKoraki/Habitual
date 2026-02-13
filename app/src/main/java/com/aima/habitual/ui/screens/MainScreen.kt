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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aima.habitual.navigation.NavGraph
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.components.BottomNavigationBar
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * MainScreen: The root UI container of the app.
 * Handles the responsive outer shell (Bottom Bar vs Navigation Rail)
 * and delegates screen routing to NavGraph.kt.
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

    // Navigation item definitions (Only show bars on these screens)
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

            // B. DELEGATE TO NAVGRAPH
            // Modifier.weight(1f) ensures it takes up all remaining space next to the Rail.
            NavGraph(
                navController = navController,
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}