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
            id = "five_expenses_one_category",
            name = "Dedicated Tracker",
            description = "You've logged 5 expenses in a single category. Keep going!",
            iconRes = R.drawable.ic_badge_tracker
        ),
        Badge(
            id = "calculator_used_3_times",
            name = "Calculator Pro",
            description = "You've used the currency calculator 3 times.",
            iconRes = R.drawable.ic_badge6_calculator
        ),
        Badge(
            id = "filter_test",
        name = "Filter Explorer",
        description = "You've filtered your expenses — keep digging!",
        iconRes = R.drawable.ic_badge6_calculator, // You can use a real icon or default one like ic_badge_default
        isUnlocked = false
        ),
       Badge(
            id = "under_budget",
    name = "Budget Master",
    description = "You stayed under budget for a category. Smart and strategic!",
    lockedDescription = "Stay under your set budget in a category to unlock this badge.",
    iconRes = R.drawable.ic_badge_under_budget
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
