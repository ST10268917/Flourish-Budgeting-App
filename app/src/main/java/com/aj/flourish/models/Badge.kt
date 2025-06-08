package com.aj.flourish.models

import com.aj.flourish.R
import com.google.firebase.Timestamp

data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val lockedDescription: String = "This badge is still locked. Unlock this by completing a task!",
    val iconRes: Int = R.drawable.ic_badge_default,  // Drawable resource ID
    val isUnlocked: Boolean = false,
    val dateUnlocked: Timestamp? = null
)