package com.aima.habitual.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Book
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.aima.habitual.R

/**
 * Defines all navigation destinations with type-safe routes,
 * localized titles, and associated icons.
 */
sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector = Icons.Default.Dashboard
) {
    // --- Bottom Navigation Destinations ---
    object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Default.Dashboard)
    object WellBeing : Screen("wellbeing", R.string.nav_wellbeing, Icons.Default.SelfImprovement)
    object Diary : Screen("diary", R.string.nav_diary, Icons.Default.Book)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)

    // --- Ritual Management ---
    object HabitDetail : Screen("habit_detail/{habitId}", R.string.add_habit_title) {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }

    object HabitStats : Screen("habit_stats/{habitId}", R.string.consistency_header) {
        fun createRoute(habitId: String) = "habit_stats/$habitId"
    }

    // --- Journaling Module ---
    object DiaryDetail : Screen("diary_detail/{entryId}", R.string.new_diary_entry) {
        fun createRoute(entryId: String) = "diary_detail/$entryId"

        val arguments = listOf(
            navArgument("entryId") { type = NavType.StringType }
        )
    }

    object DiaryView : Screen("diary_view/{entryId}", R.string.diary_header) {
        fun createRoute(entryId: String) = "diary_view/$entryId"

        val arguments = listOf(
            navArgument("entryId") { type = NavType.StringType }
        )
    }
}