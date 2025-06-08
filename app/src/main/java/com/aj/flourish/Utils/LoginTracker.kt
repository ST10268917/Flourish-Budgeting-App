package com.aj.flourish.Utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object LoginTracker {


    private const val PREFS_NAME = "login_tracker"
    private const val LAST_LOGIN_DATE = "last_login_date"
    private const val LOGIN_STREAK = "login_streak"

    suspend fun updateLoginStreak(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastLogin = prefs.getString(LAST_LOGIN_DATE, null)
        val streak = prefs.getInt(LOGIN_STREAK, 0)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastLogin == today) {
            return false // Already logged in today
        } else {
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)

            val newStreak = if (lastLogin == yesterdayStr) streak + 1 else 1

            prefs.edit()
                .putString(LAST_LOGIN_DATE, today)
                .putInt(LOGIN_STREAK, newStreak)
                .apply()

            if (newStreak >= 7) {
                BadgeManager.checkAndUnlockBadge(context, "seven_days_logged_in")
                return true
            }

            return false
        }
    }
}
