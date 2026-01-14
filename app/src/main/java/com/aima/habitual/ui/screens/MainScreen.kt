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
 * Updated MainScreen that handles adaptive layouts.
 * It uses windowSizeClass to switch between Bottom Navigation (Phone/Portrait)
 * and Navigation Rail (Tablet/Landscape).
 */
@Composable
fun MainScreen(windowSizeClass: WindowWidthSizeClass) {
    val navController = rememberNavController()

    // High Mark Logic: Determine if the screen is wide enough for a Rail
    // Compact usually means Phone in Portrait mode.
    val useNavRail = windowSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        bottomBar = {
            // Only show the BottomBar if we are NOT using the side Rail
            if (!useNavRail) {
                HabitualBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        // Use a Row if we need the side-by-side layout (Rail + Content)
        if (useNavRail) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Side Navigation Rail
                HabitualNavigationRail(navController = navController)

                // Main Content Area
                SetupNavGraph(
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Standard Portrait layout (Bottom Bar + Content)
            SetupNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}