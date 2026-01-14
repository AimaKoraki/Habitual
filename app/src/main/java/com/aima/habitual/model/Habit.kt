package com.aima.habitual.model

import java.util.UUID

/**
 * Enumeration for habit importance levels.
 */
enum class Priority { LOW, MEDIUM, HIGH }

/**
 * Data class representing a Habit.
 * Updated to include completion tracking for the Dashboard.
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String,
    val priority: Priority = Priority.MEDIUM,
    val targetDaysPerWeek: Int = 7,
    val isCompleted: Boolean = false, // Added to track daily completion status
    val createdAt: Long = System.currentTimeMillis()
)