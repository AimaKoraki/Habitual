/** Diary Entry **/
package com.aima.habitual.model

import java.util.UUID

/**
 * Represents a single entry in the user's diary.
 *
 * @property id A unique identifier for the diary entry.
 * @property title The title of the diary entry.
 * @property content The main content or body of the diary entry.
 * @property date The date the diary entry was created, in a string format.
 * @property tags A list of tags associated with the diary entry for categorization.
 * @property categoryIcon An icon representing the category of the diary entry.
 * @property timestamp The time the diary entry was created, in milliseconds since the epoch.
 */
data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: String,
    val tags: List<String> = emptyList(),
    val categoryIcon: String = "General", // For umbrella, cake, etc.
    val timestamp: Long = System.currentTimeMillis()
)