package com.aj.flourish.Utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.aj.flourish.BadgePopupActivity
import com.aj.flourish.R
import com.aj.flourish.models.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object BadgeManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun checkAndUnlockBadge(context: Context, badgeId: String, onUnlocked: (() -> Unit)? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val badge = BadgeConstants.getBadgeById(badgeId)
        if (badge == null) {
            Toast.makeText(context, "Badge not found", Toast.LENGTH_SHORT).show()
            return
        }

        val badgeRef = firestore
            .collection("users")
            .document(userId)
            .collection("badges")
            .document(badgeId)

        badgeRef.get()
            .addOnSuccessListener { document ->
                val isUnlocked = document.getBoolean("isUnlocked") ?: false

                if (!isUnlocked) {
                    val badgeData = mapOf(
                        "id" to badge.id,
                        "name" to badge.name,
                        "description" to badge.description,
                        "lockedDescription" to badge.lockedDescription,
                        "iconRes" to (badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default),
                        "isUnlocked" to true,
                        "dateUnlocked" to FieldValue.serverTimestamp()
                    )

                    badgeRef.set(badgeData, SetOptions.merge())
                        .addOnSuccessListener {
                            showBadgeUnlockedPopup(context, badge)
                            onUnlocked?.invoke()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Failed to unlock badge: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to check badge: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showBadgeUnlockedPopup(context: Context, badge: Badge) {
        val intent = Intent(context, BadgePopupActivity::class.java).apply {
            putExtra("BADGE_ID", badge.id)
            putExtra("BADGE_ICON", badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
