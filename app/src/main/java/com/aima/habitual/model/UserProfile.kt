/** UserProfile.kt **/
package com.aima.habitual.model

data class UserProfile(
    val name: String,
    val email: String,
    val joinDate: Long,
    val profilePictureUri: String?,
    val streakCount: Int // Gamification often leads to higher marks
)
