package com.aima.habitual.model

enum class Priority { LOW, MEDIUM, HIGH }

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String, // e.g., "Health", "Study"
    val priority: Priority = Priority.MEDIUM,
    val targetDaysPerWeek: Int = 7,
    val createdAt: Long = System.currentTimeMillis()
)
