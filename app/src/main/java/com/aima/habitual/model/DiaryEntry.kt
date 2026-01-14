package com.aima.habitual.model

data class DiaryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val mood: String, // e.g., "Happy", "Productive"
    val content: String,
    val attachedImagePath: String? = null // For "optimized media" requirement
)
