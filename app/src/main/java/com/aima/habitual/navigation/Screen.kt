package com.aima.habitual.navigation

/**
 * Sealed class representing all screens in the application for type-safe navigation.
 */
sealed class Screen(val route: String) {
    // Main Navigation Destinations
    object Dashboard : Screen("dashboard")
    object WellBeing : Screen("wellbeing")
    object Diary : Screen("diary")
    object Profile : Screen("profile")

    /**
     * Route for creating or editing a habit.
     * Use "new" as habitId to create a new habit.
     */
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }

    /**
     * Route for viewing habit statistics and consistency history.
     * Accepts a habitId to display specific data for that habit.
     */
    object HabitStats : Screen("habit_stats/{habitId}") {
        fun createRoute(habitId: String) = "habit_stats/$habitId"
    }
}