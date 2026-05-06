package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

/**
 * Animation and behavior settings for a companion sprite.
 */
data class CompanionMetadata(
    // Stable identifier used to match this metadata with the companion asset folder.
    @SerializedName("id") val id: String = "",
    // Frame sequences used by the sprite animator for movement and turning.
    @SerializedName("walkFrames") val walkFrames: List<String> = listOf("walk_0.png", "walk_1.png"),
    @SerializedName("turnFrames") val turnFrames: List<String> = emptyList(),
    @SerializedName("idleFrames") val idleFrames: List<String> = listOf("idle_0.png"),
    // Timing values control how long each frame or idle state is displayed.
    @SerializedName("walkFrameDurationMs") val walkFrameDurationMs: Long = 200L,
    @SerializedName("idleFrameDurationMs") val idleFrameDurationMs: Long = 150L,
    // Idle tuning keeps the companion from moving constantly and makes motion feel natural.
    @SerializedName("idleChanceOnEdge") val idleChanceOnEdge: Float = 0.45f,
    @SerializedName("idleMinMs") val idleMinMs: Long = 800L,
    @SerializedName("idleMaxMs") val idleMaxMs: Long = 3500L
)
