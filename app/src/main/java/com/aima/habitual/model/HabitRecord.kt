package com.aima.habitual.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single completion instance of a habit.
 * Connects habits to the Calendar and Consistency charts.
 * Stored as a Room Entity in the local database.
 */
@Entity(tableName = "habit_records")
data class HabitRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Reference to the associated Habit
    val habitId: String,

    // Epoch day for historical tracking
    val timestamp: Long,

    // Completion state for the Dashboard UI
    val isCompleted: Boolean
)