package com.aima.habitual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aima.habitual.navigation.SetupNavGraph
import com.aima.habitual.ui.components.BottomNavigationBar
import com.aima.habitual.ui.theme.HabitualTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge configuration for a modern immersive UI
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            // Theme state management
            var isDarkTheme by remember { mutableStateOf(false) }

            HabitualTheme(darkTheme = isDarkTheme) {
                // The NavController is the central point for screen transitions
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        // Provides the 4-tab menu at the bottom
                        BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    // The NavGraph manages the screens and the shared HabitViewModel
                    SetupNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}