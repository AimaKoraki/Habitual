package com.aima.habitual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import com.aima.habitual.ui.screens.MainScreen
import com.aima.habitual.ui.theme.HabitualTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // EXPLICIT INITIALIZATION: No more "auto" guessing
        enableEdgeToEdge(
            // .dark() forces light icons (Time, Battery) for your Deep Teal bar
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            // .light() forces dark icons (Back/Home) for your Off-white nav bar
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            HabitualTheme(darkTheme = isDarkTheme) {
                val windowSize = calculateWindowSizeClass(this)
                MainScreen(
                    windowSizeClass = windowSize.widthSizeClass,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}