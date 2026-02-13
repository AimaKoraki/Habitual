/** HabitRecord.kt **/
package com.aima.habitual.model

import java.util.UUID

/**
 * HabitRecord tracks the historical completion of a specific habit on a specific day.
 * This data drives the "History" Calendar and "Consistency" Charts.
 */
data class HabitRecord(
    // Unique ID for the specific record entry
    val id: String = UUID.randomUUID().toString(),

    // Link to the parent habit
    val habitId: String,

    // The timestamp for the day this record belongs to
    val timestamp: Long,

    // Status for the "Checkmark" button on the Dashboard
    val isCompleted: Boolean,

    /**
     * Optional value for habits that track metrics (e.g., "5km" or "8 glasses of water").
     * This drives the bar heights in your Consistency Chart.
     */
    val completionValue: Float? = null
)