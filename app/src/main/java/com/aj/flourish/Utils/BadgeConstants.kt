package com.aj.flourish.Utils

import com.aj.flourish.R
import com.aj.flourish.models.Badge

object  BadgeConstants {


    val allBadges = listOf(
        Badge(
            id = "first_budget",
            name = "First Budget Set",
            description = "Congratulations on setting your very first budget!",
            iconRes = R.drawable.ic_badge3_first_budget
        ),
        Badge(
            id = "first_transaction",
            name = "First Transaction Logged",
            description = "You logged your first expense or income. Keep going!",
            iconRes = R.drawable.ic_badge2_first_transaction
        ),
        Badge(
            id = "save_500",
            name = "Saved R500",
            description = "You saved R500! Your savings journey is off to a great start.",
            iconRes = R.drawable.ic_badge4_save_500
        ),
        Badge(
            id = "seven_days_logged_in",
            name = "7 Days Logged In",
            description = "You've logged in for 7 days straight. Amazing consistency!",
            iconRes = R.drawable.ic_badge1_7_days
        ),
        Badge(
            id = "no_overspending_week",
            name = "No Overspending This Week",
            description = "You stayed within your budget this week. Great discipline!",
            iconRes = R.drawable.ic_badge5_no_overspend
        ),
        Badge(
            id = "calculator_used_3_times",
            name = "Calculator Pro",
            description = "You've used the currency calculator 3 times.",
            iconRes = R.drawable.ic_badge6_calculator
        )
        // Add the code for more badges here


    )
    fun getBadgeById(id: String): Badge? {
        return allBadges.find { it.id == id }
    }

    val lockedBadge = Badge(
        id = "locked",
        name = "Locked Badge",
        description = "Unlock this by completing a task!",
        lockedDescription = "You haven't unlocked this badge yet.",
        iconRes = R.drawable.ic_badge_default,
        isUnlocked = false
    )
}
