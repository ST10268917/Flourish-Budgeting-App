package com.aj.flourish.Utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.aj.flourish.BadgePopupActivity
import com.aj.flourish.R
import com.aj.flourish.models.Badge
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object BadgeManager {

    suspend fun checkAndUnlockBadge(context: Context, badgeId: String) {
        val badge = BadgeConstants.getBadgeById(badgeId) ?: run {
            Log.w("BadgeManager", "Badge with ID $badgeId not found.")
            return
        }

        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Log.w("BadgeManager", "No logged-in user found.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userBadgesRef = db.collection("users").document(user.uid).collection("badges")

        try {
            val snapshot = userBadgesRef.get().await()
            val existingIds = snapshot.documents.map { it.id }

            if (!existingIds.contains(badgeId)) {
                val unlockedBadge = badge.copy(
                    isUnlocked = true,
                    dateUnlocked = Timestamp.now()
                )

                userBadgesRef.document(badgeId).set(unlockedBadge).await()

                withContext(Dispatchers.Main) {
                    Log.d("BadgeManager", "Badge unlocked: ${badge.name}")
                    showBadgePopup(context, unlockedBadge)
                }
            } else {
                Log.d("BadgeManager", "Badge $badgeId already unlocked.")
            }

        } catch (e: Exception) {
            Log.e("BadgeManager", "Failed to unlock badge $badgeId: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error unlocking badge: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBadgePopup(context: Context, badge: Badge) {
        val intent = Intent(context, BadgePopupActivity::class.java).apply {
            putExtra("BADGE_ID", badge.id)
            putExtra("BADGE_ICON", badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default)

            // Add flags if context is not an activity
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        context.startActivity(intent)
    }
}

