/** BottomNavigationBar.kt **/

package com.aima.habitual.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {

    // 1. Define the tabs to display in the bar.
    // These come from your sealed class 'Screen' in navigation/Screen.kt
    val items = listOf(
        Screen.Dashboard,
        Screen.WellBeing,
        Screen.Diary,
        Screen.Profile
    )

    // 2. Observe the current back stack entry.
    // This makes the UI re-compose (update) whenever the user navigates to a new screen.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 3. Determine if the bar should be visible.
    // We only want to show the bottom bar on the top-level screens,
    // not on detail screens (like a hypothetical "Edit Habit" screen).
    val mainRoutes = items.map { it.route }
    val showBottomBar = currentDestination?.route in mainRoutes

    if (showBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface, // Matches the Theme (White or Dark Grey)
            tonalElevation = HabitualTheme.components.navBarElevation // Adds a subtle shadow/tint to separate it from the content
        ) {
            // 4. Loop through each screen item to create a tab
            items.forEach { screen ->

                // Check if this specific tab is currently active.
                // hierarchy check ensures it stays selected even if we are in a sub-route of this tab.
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                // Retrieve the localized title (e.g., "Home", "Journal") from strings.xml
                val labelText = stringResource(id = screen.titleRes)

                NavigationBarItem(
                    // Icon logic
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = labelText
                        )
                    },
                    // Text label below the icon
                    label = {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    // Highlight state
                    selected = isSelected,

                    // 5. Navigation Logic (Crucial for proper tab behavior)
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true // Save the state of the screen we are leaving
                            }
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    },
                    // 6. Custom Colors (Forest Green & Sage Theme)
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary, // Forest Green (Active Icon)
                        selectedTextColor = MaterialTheme.colorScheme.primary, // Forest Green (Active Text)
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer, // Light Sage (Active Background Pill)
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, // Grey (Inactive)
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}