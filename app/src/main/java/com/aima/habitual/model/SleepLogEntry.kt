package com.aima.habitual.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a manual sleep log entry provided by the user.
 * Stored as a Room Entity in the local database.
 */
@Entity(tableName = "sleep_log_entries")
data class SleepLogEntry(
    @PrimaryKey
    val dateEpoch: Long,
    val durationMinutes: Int,
    val quality: String
)
