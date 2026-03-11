package com.aima.habitual.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single entry in the user's diary.
 * Stored as a Room Entity in the local database.
 *
 * @property id A unique identifier for the diary entry.
 * @property title The title of the diary entry.
 * @property content The main content or body of the diary entry.
 * @property tags A list of tags associated with the diary entry for categorization.
 * @property timestamp The time the diary entry was created, in milliseconds since the epoch.
 */
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false,
    val mood: String? = null,
    val photoUri: String? = null,
    val audioFilePath: String? = null,
    val locationText: String? = null
)