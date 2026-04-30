package com.aima.habitual.model

import com.google.gson.annotations.SerializedName

/**
 * CompanionResponse: Represents the root JSON object from the external JSON file.
 * The raw file has the format: { "pets": [ ... ] }
 */
data class CompanionResponse(
    @SerializedName("pets") val pets: List<Companion>
)

/**
 * Companion: Represents a virtual companion/pet unlocked by the user.
 * Mapped to the elements inside the "pets" array in the JSON file.
 *
 * Example JSON object:
 * {
 *   "name" : "Purrsloud",
 *   "species" : "Cat",
 *   "favFoods" : ["wet food", "dry food", "<strong>any</strong> food"],
 *   "birthYear" : 2016,
 *   "photo" : "https://learnwebcode.github.io/json-example/images/cat-2.jpg"
 * }
 */
data class Companion(
    @SerializedName("name") val name: String,
    @SerializedName("species") val species: String,
    @SerializedName("birthYear") val birthYear: Int,
    @SerializedName("photo") val photoUrl: String,
    @SerializedName("favFoods") val favoriteFoods: List<String>? = emptyList()
)
