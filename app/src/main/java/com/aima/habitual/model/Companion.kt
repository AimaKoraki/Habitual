package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

/**
 * A virtual companion that can be loaded from JSON and shown in the app.
 */
data class VirtualCompanion(
    // These field names map directly to the bundled JSON structure.
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String,
    @SerializedName("requiredLevel") val requiredLevel: Int = 1,
    @SerializedName("spriteAsset") val spriteAsset: String,
    // Unlock state is managed locally by the app.
    val unlockStatus: Boolean = false,
    @SerializedName("birthYear") val birthYear: Int? = null,
    @SerializedName("favFoods") val favoriteFoods: List<String>? = null
)

/**
 * Wrapper object used when parsing the top-level JSON payload.
 */
data class CompanionListResponse(
    @SerializedName("companions") val companions: List<VirtualCompanion>
)
