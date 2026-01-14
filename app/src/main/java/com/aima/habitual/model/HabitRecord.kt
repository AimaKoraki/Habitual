package com.aima.habitual.model

data class HabitRecord(
    val habitId: String,
    val date: Long, // Use timestamp for the specific day
    val isCompleted: Boolean,
    val completionValue: Float? = null // Useful for Wellbeing (e.g., "walked 5km")
)
