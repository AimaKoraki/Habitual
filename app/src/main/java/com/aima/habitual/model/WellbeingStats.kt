package com.aima.habitual.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Snapshot of daily health metrics for tracking and visualization.
 */
data class WellbeingStats(
    val stepsCount: Int = 0,
    val sleepDurationHours: Double = 0.0,
    val waterIntakeMl: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Converts the sync timestamp into a human-readable UI string (e.g., "8:30 PM").
     */
    val formattedLastSync: String
        get() = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
}