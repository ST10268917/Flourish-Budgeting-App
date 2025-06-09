package com.aj.flourish.Utils

import android.content.Context
import android.widget.Toast
import com.aj.flourish.BadgePopupActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object LoginTracker {

    private const val PREFS_NAME = "login_tracker"
    private const val LAST_LOGIN_DATE = "last_login_date"
    private const val LOGIN_STREAK = "login_streak"

     fun updateLoginStreak(context: Context, onBadgeUnlocked: (() -> Unit)? = null): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastLogin = prefs.getString(LAST_LOGIN_DATE, null)
        val streak = prefs.getInt(LOGIN_STREAK, 0)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Already logged in today
        if (lastLogin == today) {
            return streak
        }

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)

        val newStreak = if (lastLogin == yesterdayStr) streak + 1 else 1

        prefs.edit()
            .putString(LAST_LOGIN_DATE, today)
            .putInt(LOGIN_STREAK, newStreak)
            .apply()

        if (newStreak >= 7) {
            CoroutineScope(Dispatchers.IO).launch {
                BadgeManager.checkAndUnlockBadge(context, "seven_days_logged_in")
                // Animation / popup on UI thread
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "🎉 You've unlocked the 7-Day Login Streak badge!", Toast.LENGTH_LONG).show()
                    onBadgeUnlocked?.invoke()
                }
            }
        }

        return newStreak
    }
}
