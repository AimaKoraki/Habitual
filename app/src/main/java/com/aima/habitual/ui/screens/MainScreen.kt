package com.aima.habitual.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aima.habitual.navigation.SetupNavGraph
import com.aima.habitual.ui.components.HabitualBottomBar
import com.aima.habitual.ui.components.HabitualNavigationRail

/**
 * Updated MainScreen that handles adaptive layouts and theme state passing.
 */
@Composable
fun MainScreen(
    windowSizeClass: WindowWidthSizeClass,
    isDarkTheme: Boolean,            // New parameter for theme state
    onThemeChange: (Boolean) -> Unit // New parameter for theme toggle logic
) {
    val navController = rememberNavController()

    // High Mark Logic: Determine if the screen is wide enough for a Rail
    val useNavRail = windowSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        bottomBar = {
            if (!useNavRail) {
                HabitualBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        if (useNavRail) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HabitualNavigationRail(navController = navController)

                // Pass theme parameters into the Navigation Graph
                SetupNavGraph(
                    navController = navController,
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                )
            }
        } else {
            SetupNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}