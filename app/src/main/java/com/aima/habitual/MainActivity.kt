package com.aima.habitual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.aima.habitual.ui.screens.MainScreen
import com.aima.habitual.ui.theme.HabitualTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables drawing behind the status bar and navigation bar for a modern look
        enableEdgeToEdge()

        setContent {
            HabitualTheme {
                // Measure the current window size (Width and Height)
                val windowSize = calculateWindowSizeClass(this)

                // Launch the main entry point of your UI,
                // passing the width class to handle orientation/responsive layouts.
                MainScreen(windowSizeClass = windowSize.widthSizeClass)
            }
        }
    }
}