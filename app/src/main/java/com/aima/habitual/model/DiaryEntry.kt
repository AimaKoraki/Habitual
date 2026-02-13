/** Diary Entry **/
package com.aima.habitual.model

import java.util.UUID

data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: String,
    val tags: List<String> = emptyList(),
    val categoryIcon: String = "General", // For umbrella, cake, etc.
    val timestamp: Long = System.currentTimeMillis()
)