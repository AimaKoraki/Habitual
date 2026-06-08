package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.aima.habitual.ui.theme.AppTheme

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    var isDarkTheme by mutableStateOf(prefs.getBoolean("is_dark_theme", false))
        private set

    var appTheme by mutableStateOf(
        AppTheme.valueOf(prefs.getString("app_theme", AppTheme.GREEN.name) ?: AppTheme.GREEN.name)
    )
        private set

    fun toggleTheme(isDark: Boolean) {
        isDarkTheme = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun changeAppTheme(theme: AppTheme) {
        appTheme = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    var stepGoal by mutableStateOf(prefs.getInt("daily_step_goal", 10000))
        private set

    var waterGoal by mutableStateOf(prefs.getInt("daily_water_goal_ml", 2000))
        private set

    fun updateStepGoal(newGoal: Int) {
        if (newGoal > 0) {
            stepGoal = newGoal
            prefs.edit().putInt("daily_step_goal", newGoal).apply()
        }
    }

    fun updateWaterGoal(newGoalMl: Int) {
        if (newGoalMl > 0) {
            waterGoal = newGoalMl
            prefs.edit().putInt("daily_water_goal_ml", newGoalMl).apply()
        }
    }
}
