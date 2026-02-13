package com.aima.habitual.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aima.habitual.navigation.Screen

/**
 * HabitualBottomBar pulls labels and icons directly from the Screen sealed class.
 * This removes the need for a separate NavItem data class.
 */
@Composable
fun HabitualBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Pulling destinations from Screen.kt
    val items = listOf(
        Screen.Dashboard,
        Screen.WellBeing,
        Screen.Diary,
        Screen.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, // Soft Sage
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        items.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val labelText = stringResource(id = screen.titleRes) // Uses clean labels like "Rituals"

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = labelText) },
                label = { Text(labelText, style = MaterialTheme.typography.labelSmall) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, // Forest Green
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer // Light Sage
                ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * HabitualNavigationRail provides adaptive navigation for larger screens.
 */
@Composable
fun HabitualNavigationRail(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Dashboard,
        Screen.WellBeing,
        Screen.Diary,
        Screen.Profile
    )

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface, // Soft Sage
        header = { /* Optional: Add a Forest Green Logo icon here */ }
    ) {
        items.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val labelText = stringResource(id = screen.titleRes)

            NavigationRailItem(
                icon = { Icon(screen.icon, contentDescription = labelText) },
                label = { Text(labelText) },
                selected = isSelected,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, // Forest Green
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer // Light Sage
                ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}