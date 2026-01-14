package com.aima.habitual.model

data class WellbeingStats(
    val stepsCount: Int,
    val sleepDurationHours: Float,
    val waterIntakeMl: Int,
    val lastSyncTimestamp: Long
)
