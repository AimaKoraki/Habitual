package com.aima.habitual.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Book
import com.aima.habitual.R

/**
 * Sealed class representing all screens in the application for type-safe navigation.
 * Updated to use String Resource IDs for localized titles.
 */
sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int, // Changed from String to Int for strings.xml support
    val icon: ImageVector = Icons.Default.Dashboard
) {
    // Main Navigation Destinations using nav_* strings
    object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Default.Dashboard)
    object WellBeing : Screen("wellbeing", R.string.nav_wellbeing, Icons.Default.SelfImprovement)
    object Diary : Screen("diary", R.string.nav_diary, Icons.Default.Book)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)

    /** * Route for creating or editing a habit.
     */
    object HabitDetail : Screen("habit_detail/{habitId}", R.string.add_habit_title) {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }

    /**
     * Route for viewing habit statistics and consistency history.
     */
    object HabitStats : Screen("habit_stats/{habitId}", R.string.consistency_header) {
        fun createRoute(habitId: String) = "habit_stats/$habitId"
    }
    object DiaryDetail : Screen("diary_detail/{entryId}", R.string.new_diary_entry) {
        fun createRoute(entryId: String) = "diary_detail/$entryId"

        // Define arguments for the NavGraph to parse
        val arguments = listOf(
            androidx.navigation.navArgument("entryId") {
                type = androidx.navigation.NavType.StringType
            }
        )
    }
    // Inside Screen.kt
    object DiaryView : Screen("diary_view/{entryId}", R.string.diary_header) {
        fun createRoute(entryId: String) = "diary_view/$entryId"

        val arguments = listOf(
            androidx.navigation.navArgument("entryId") {
                type = androidx.navigation.NavType.StringType
            }
        )
    }
}