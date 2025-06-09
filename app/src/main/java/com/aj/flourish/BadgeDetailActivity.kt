package com.aj.flourish

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aj.flourish.Utils.BadgeConstants
import com.aj.flourish.R
import com.aj.flourish.base.BaseActivity
import java.text.SimpleDateFormat
import java.util.*

class BadgeDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_detail)

        val badgeId = intent.getStringExtra("BADGE_ID")

        val badge = BadgeConstants.getBadgeById(badgeId ?: "")

        val badgeIcon = findViewById<ImageView>(R.id.badgeIcon)
        val badgeName = findViewById<TextView>(R.id.badgeName)
        val badgeDescription = findViewById<TextView>(R.id.badgeDescription)
        val dateText = findViewById<TextView>(R.id.dateTextView)
        val backButton = findViewById<Button>(R.id.backButton)

        if (badge != null) {
            val iconRes = badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default
            badgeIcon.setImageResource(iconRes)
            badgeIcon.contentDescription = "Badge Icon: ${badge.name}"

            // Animate elements
            val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            badgeIcon.startAnimation(fadeIn)
            badgeName.startAnimation(fadeIn)
            badgeDescription.startAnimation(fadeIn)

            badgeName.text = badge.name

            if (badge.isUnlocked) {
                badgeDescription.text = badge.description
                badgeIcon.alpha = 1f
                badge.dateUnlocked?.let {
                    val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it.toDate())
                    dateText.text = "Unlocked on: $formattedDate"
                }
            } else {
                badgeDescription.text = badge.lockedDescription
                badgeIcon.alpha = 0.4f
                dateText.text = "Locked"
            }

            backButton.setOnClickListener { finish() }

        } else {
            Toast.makeText(this, "Badge not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
