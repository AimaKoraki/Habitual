package com.aima.habitual.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single completion instance of a habit.
 * Connects habits to the Calendar and Consistency charts.
 * Stored as a Room Entity in the local database.
 */
@Entity(
    tableName = "habit_records",
    foreignKeys = [ForeignKey(
        entity = Habit::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId", "timestamp"])]
)
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