package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

data class VirtualCompanion(
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String,
    @SerializedName("requiredLevel") val requiredLevel: Int = 1,
    @SerializedName("spriteAsset") val spriteAsset: String,
    val unlockStatus: Boolean = false,
    @SerializedName("birthYear") val birthYear: Int? = null,
    @SerializedName("favFoods") val favoriteFoods: List<String>? = null
)

data class CompanionListResponse(
    @SerializedName("companions") val companions: List<VirtualCompanion>
)
