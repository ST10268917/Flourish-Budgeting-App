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
import java.text.SimpleDateFormat
import java.util.*

class BadgeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge_detail)

        val badgeId = intent.getStringExtra("BADGE_ID") ?: return
        val badge = BadgeConstants.allBadges.find { it.id == badgeId }

        if (badge != null) {
            val badgeIcon = findViewById<ImageView>(R.id.badgeIcon)
            val badgeName = findViewById<TextView>(R.id.badgeName)
            val badgeDescription = findViewById<TextView>(R.id.badgeDescription)
            val dateText = findViewById<TextView>(R.id.dateTextView)
            val backButton = findViewById<Button>(R.id.backButton)

            val iconRes = badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default
            badgeIcon.setImageResource(iconRes)
            badgeIcon.contentDescription = "Badge Icon: ${badge.name}" // Accessibility

            // Fade-in animation
            val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            badgeIcon.startAnimation(animation)

            badgeName.text = badge.name
            badgeDescription.text = badge.description

            // Show unlocked date if available
            badge.dateUnlocked?.let {
                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it.toDate())
                dateText.text = "Unlocked on: $formattedDate"
            }

            backButton.setOnClickListener {
                finish()
            }
        } else {
            Toast.makeText(this, "Badge not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
