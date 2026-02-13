/** UserProfile.kt **/
package com.aima.habitual.model

/**
 * Represents the authenticated user's identity and progress.
 */
data class UserProfile(
    val name: String,
    val email: String,

    // Account creation timestamp for "Member Since" displays
    val joinDate: Long,

    // Persistent link to the user's selected avatar image
    val profilePictureUri: String?,

    // Total consecutive days active (used for gamification and badges)
    val streakCount: Int
)