package com.aima.habitual

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.aima.habitual.ui.screens.MainScreen
import com.aima.habitual.ui.theme.HabitualTheme

class MainActivity : FragmentActivity() {

    // 1. Permission Launcher: Required for Step Counter on Android 10+ (API 29+)
    // Also used for Notifications on Android 13+ (API 33+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle gracefully: if they deny, steps/reminders just don't work
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Request Permissions: Ask user immediately on app launch
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        // 3. Edge-to-Edge: Configured for your "Pure White" theme
        // We use 'SystemBarStyle.light' to ensure status bar icons (time/battery) are Dark
        // so they are visible against your white background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT, // Scrim
                android.graphics.Color.TRANSPARENT  // Dark Scrim
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            // 4. IMPROVEMENT: Use 'rememberSaveable'
            // This ensures the Theme doesn't reset to Light Mode if the user rotates the screen.
            // 4. IMPROVEMENT: Use ViewModel for Theme Persistence
            // This ensures the Theme is saved even if the app is killed.
            val habitViewModel: com.aima.habitual.viewmodel.HabitViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val authViewModel: com.aima.habitual.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val settingsViewModel: com.aima.habitual.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val wellbeingViewModel: com.aima.habitual.viewmodel.WellbeingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val diaryViewModel: com.aima.habitual.viewmodel.DiaryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            LaunchedEffect(Unit) {
                authViewModel.onUserChanged = {
                    wellbeingViewModel.clearCache()
                }
                diaryViewModel.onAddSteps = { steps, date ->
                    wellbeingViewModel.addSteps(steps, date)
                }
                habitViewModel.onAddSteps = { steps, date ->
                    wellbeingViewModel.addSteps(steps, date)
                }
            }

            val isDarkTheme = settingsViewModel.isDarkTheme
            val appTheme = settingsViewModel.appTheme

            val windowSizeClass = calculateWindowSizeClass(this)

            HabitualTheme(darkTheme = isDarkTheme, appTheme = appTheme) {
                MainScreen(
                    windowSizeClass = windowSizeClass.widthSizeClass,
                    isDarkTheme = isDarkTheme,
                    appTheme = appTheme,
                    onThemeChange = { settingsViewModel.toggleTheme(it) },
                    onThemeColorChange = { settingsViewModel.changeAppTheme(it) },
                    habitViewModel = habitViewModel,
                    authViewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    wellbeingViewModel = wellbeingViewModel,
                    diaryViewModel = diaryViewModel
                )
            }
        }
    }
}