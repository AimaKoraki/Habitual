/** HabitRecord.kt **/
package com.aima.habitual.model

import java.util.UUID

/**
 * Represents a single completion instance of a habit.
 * Connects habits to the Calendar and Consistency charts.
 */
data class HabitRecord(
    val id: String = UUID.randomUUID().toString(),

    // Reference to the associated Habit
    val habitId: String,

    // Epoch day/timestamp for historical tracking
    val timestamp: Long,

    // Completion state for the Dashboard UI
    val isCompleted: Boolean
)