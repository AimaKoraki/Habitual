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

    val items = listOf(
        Screen.Dashboard,
        Screen.WellBeing,
        Screen.Diary,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = HabitualTheme.elevation.medium
    ) {
        items.forEach { screen ->

            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val labelText = stringResource(id = screen.titleRes)

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = labelText
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
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
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
