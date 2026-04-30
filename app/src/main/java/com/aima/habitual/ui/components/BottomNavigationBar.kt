/** BottomNavigationBar.kt **/

package com.aima.habitual.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aima.habitual.navigation.Screen
import com.aima.habitual.ui.theme.HabitualTheme

/**
 * BottomNavigationBar: The main navigation control for the app.
 * It sits at the bottom of the screen and allows switching between the
 * four main sections: Dashboard, Wellbeing, Diary, and Profile.
 *
 * Visibility is controlled by MainScreen — this composable always renders
 * when called.
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {

    // 1. Define the top-level screens accessible from the Bottom Bar
    val items = listOf(
        Screen.Dashboard,
        Screen.WellBeing,
        Screen.Diary,
        Screen.Profile
    )

    // 2. Observe the current back stack to track where the user is in the app
    // This allows the bar to highlight the correct icon automatically.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        // Material3 surface styling for a modern, clean look
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = HabitualTheme.elevation.medium
    ) {
        items.forEach { screen ->

            // 3. Determine if this specific item is currently selected
            // We check the destination hierarchy to ensure parent routes are matched correctly.
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val labelText = stringResource(id = screen.titleRes)

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = labelText // Accessibility support for Screen Readers
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    // 4. Handle Navigation Logic
                    navController.navigate(screen.route) {
                        // Pop up to the start destination to avoid building a massive backstack
                        // when switching between main tabs.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Preserves scroll position and text field state
                        }

                        // Avoid multiple copies of the same destination when re-selecting the same tab
                        launchSingleTop = true

                        // Restore previous state when re-selecting a previously visited tab
                        restoreState = true
                    }
                },
                // 5. Define Theme-aware Colors
                // Ensures accessibility contrast ratios are met for both Light and Dark themes.
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}