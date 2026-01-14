package com.aima.habitual.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object WellBeing : Screen("wellbeing")
    object Diary : Screen("diary")
    object Profile : Screen("profile")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
}