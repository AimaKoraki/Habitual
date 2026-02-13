package com.aima.habitual.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WellbeingStats represents a snapshot of the user's daily health metrics.
 */
data class WellbeingStats(
    val stepsCount: Int = 0,
    val sleepDurationHours: Double = 0.0,
    val waterIntakeMl: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Helper to return a formatted string for the UI (e.g., "8:30 PM")
     */
    val formattedLastSync: String
        get() = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
}