package com.aima.habitual.model

/**
 * Represents a manual sleep log entry provided by the user.
 * Serialized to JSON via Gson and stored in SharedPreferences.
 */
data class SleepLogEntry(
    val dateEpoch: Long,
    val durationMinutes: Int,
    val quality: String
)
