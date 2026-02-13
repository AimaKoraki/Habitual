/** Habit.kt **/
package com.aima.habitual.model

import java.util.UUID

/**
 * Enumeration for habit importance levels.
 */
enum class Priority { LOW, MEDIUM, HIGH }

/**
 * Data class representing a Habit.
 * Fully optimized for Dashboard sorting, Monthly Targets, and Repeat Days.
 */
data class Habit(
    // Unique identifier for database and navigation keys
    val id: String = UUID.randomUUID().toString(),

    val title: String,
    val description: String = "",
    val category: String,
    val priority: Priority = Priority.MEDIUM,

    // Stores selected days (e.g., [0, 2, 4] for Sun, Tue, Thu)
    val repeatDays: List<Int> = emptyList(),

    // Represents the long-term goal (e.g., 3 months)
    val targetMonths: Int = 1,

    // Drives the grey-out and sorting logic on the Dashboard
    val isCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)