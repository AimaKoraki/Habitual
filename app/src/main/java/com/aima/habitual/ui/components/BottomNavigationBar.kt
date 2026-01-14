package com.aima.habitual.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aima.habitual.navigation.Screen

/**
 * BottomNavigationBar provides the main navigation hub for the app.
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Dashboard to Icons.Default.Dashboard,
        Screen.WellBeing to Icons.Default.SelfImprovement,
        Screen.Diary to Icons.Default.Book,
        Screen.Profile to Icons.Default.Person
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show the bottom bar if the current screen is one of the main 4
    val mainRoutes = items.map { it.first.route }
    val showBottomBar = currentDestination?.route in mainRoutes

    if (showBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            items.forEach { (screen, icon) ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = screen.route) },
                    label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Standard navigation behavior: avoid multiple copies of same screen
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF004D40), // Your Deep Teal
                        indicatorColor = Color(0xFF004D40).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}