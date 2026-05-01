package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

data class CompanionMetadata(
    @SerializedName("id") val id: String = "",
    @SerializedName("walkFrames") val walkFrames: List<String> = listOf("walk_0.png", "walk_1.png"),
    @SerializedName("turnFrames") val turnFrames: List<String> = emptyList(),
    @SerializedName("idleFrames") val idleFrames: List<String> = listOf("idle_0.png"),
    @SerializedName("walkFrameDurationMs") val walkFrameDurationMs: Long = 200L,
    @SerializedName("idleFrameDurationMs") val idleFrameDurationMs: Long = 150L,
    @SerializedName("idleChanceOnEdge") val idleChanceOnEdge: Float = 0.45f,
    @SerializedName("idleMinMs") val idleMinMs: Long = 800L,
    @SerializedName("idleMaxMs") val idleMaxMs: Long = 3500L
)
