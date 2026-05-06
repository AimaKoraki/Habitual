package com.aima.habitual.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.aima.habitual.ui.components.ShimejiOverlay
import com.aima.habitual.ui.theme.AppTheme
import com.aima.habitual.ui.theme.HabitualTheme
import com.aima.habitual.utils.ConnectivityStatus
import com.aima.habitual.viewmodel.CompanionViewModel
import com.aima.habitual.viewmodel.HabitViewModel

/**
 * MainScreen: The root UI container of the app.
 * Handles the responsive outer shell (Bottom Bar vs Navigation Rail), delegates
 * screen routing to NavGraph.kt, and renders the Shimeji-style walking companion
 * overlay above the BottomNavigationBar on all main tabs.
 */
@Composable
fun MainScreen(
    windowSizeClass: WindowWidthSizeClass,
    isDarkTheme: Boolean,
    appTheme: AppTheme,
    onThemeChange: (Boolean) -> Unit,
    onThemeColorChange: (AppTheme) -> Unit,
    viewModel: HabitViewModel
) {
    // ── Tokens ─────────────────────────────────
    val statusBarGap = HabitualTheme.spacing.statusBarGap
    // ────────────────────────────────────────────

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val networkStatus by viewModel.networkStatus.collectAsState()

    LaunchedEffect(networkStatus) {
        if (networkStatus == ConnectivityStatus.Lost || networkStatus == ConnectivityStatus.Unavailable) {
            snackbarHostState.showSnackbar(
                message = "No Internet Connection",
                duration = SnackbarDuration.Indefinite
            )
        } else if (networkStatus == ConnectivityStatus.Available) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val showSideRail = windowSizeClass != WindowWidthSizeClass.Compact || isLandscape

    val mainTabs = listOf(Screen.Dashboard, Screen.WellBeing, Screen.Diary, Screen.Companions, Screen.Profile)
    val showBars = currentRoute in mainTabs.map { it.route }

    // Hoist the CompanionViewModel here so the overlay and the Companion screens
    // share the same instance — toggling "active" in the detail screen updates the
    // overlay live without round-tripping through SharedPreferences.
    val companionViewModel: CompanionViewModel = viewModel()
    val activeName by companionViewModel.activeCompanionName.collectAsState()
    val companions by companionViewModel.companions.collectAsState()
    val activeCompanion = companions.firstOrNull { it.name == activeName && it.unlockStatus }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!showSideRail && showBars && viewModel.isLoggedIn) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(top = statusBarGap)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
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

                NavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    companionViewModel = companionViewModel,
                    isDarkTheme = isDarkTheme,
                    appTheme = appTheme,
                    onThemeChange = onThemeChange,
                    onThemeColorChange = onThemeColorChange,
                    modifier = Modifier.weight(1f)
                )
            }

            // Walking-companion overlay. Pinned to the bottom of the Scaffold content
            // area, which sits directly above the BottomNavigationBar (if present),
            // or at the very bottom of the screen if there is no bottom bar.
            // The overlay installs no clickable / pointerInput, so taps fall through.
            if (viewModel.isLoggedIn) {
                ShimejiOverlay(
                    activeCompanion = activeCompanion,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
