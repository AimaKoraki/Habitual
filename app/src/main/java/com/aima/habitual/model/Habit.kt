package com.aima.habitual.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Defines the priority levels for a habit.
 */
enum class Priority { LOW, MEDIUM, HIGH }

/**
 * Represents a user-defined habit.
 * Stored as a Room Entity in the local database.
 *
 * @property id Unique identifier for the habit.
 * @property title The name of the habit.
 * @property description A brief description of the habit.
 * @property category The category the habit belongs to.
 * @property priority The importance level of the habit.
 * @property repeatDays A list of integers representing the days of the week to repeat the habit (e.g., 0 for Sunday).
 * @property targetMonths The duration in months for which the user wants to follow the habit.
 * @property reminderTime The time (HH:mm) at which the user wants to be reminded.
 * @property isReminderEnabled Whether the reminder notification is active.
 * @property createdAt The timestamp of when the habit was created.
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val category: String,
    val priority: Priority = Priority.MEDIUM,
    val repeatDays: List<Int> = emptyList(),
    val targetMonths: Int = 1,
    val reminderTime: String? = null,
    val isReminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
