package com.aj.flourish

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aj.flourish.Utils.BadgeConstants
import com.aj.flourish.base.BaseActivity
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class BadgePopupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_badge_unlocked)

        val badgeIcon: ImageView = findViewById(R.id.badge_icon)
        val badgeTitle: TextView = findViewById(R.id.badge_title)
        val badgeDescription: TextView = findViewById(R.id.badge_description)
        val btnClose: Button = findViewById(R.id.btn_close)
        val konfettiView: KonfettiView = findViewById(R.id.konfettiView)

        val badgeId = intent.getStringExtra("BADGE_ID")
        val badge = badgeId?.let { BadgeConstants.getBadgeById(it) }

        if (badge != null) {
            val iconRes = badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default
            badgeIcon.setImageResource(iconRes)
            badgeIcon.contentDescription = "Badge icon: ${badge.name}"
            badgeTitle.text = "🎉 Badge Unlocked!"
            badgeDescription.text = "${badge.name}\n\n${badge.description}"

            // 🔆 Show glow only if badge is unlocked
            val glowBackground = findViewById<ImageView>(R.id.glowBackground)
            if (badge.isUnlocked) {
                glowBackground.visibility = View.VISIBLE
            } else {
                glowBackground.visibility = View.GONE
            }
        } else {
            badgeIcon.setImageResource(R.drawable.ic_badge_default)
            badgeTitle.text = "Badge Unlocked!"
            badgeDescription.text = "You've earned a badge!"
        }


        // Optional animation for badge icon
        val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        badgeIcon.startAnimation(scaleIn)

        // Load animation
        val pulse = AnimationUtils.loadAnimation(this, R.anim.pulse)
        val scale = AnimationUtils.loadAnimation(this, R.anim.scale_up_center)
        badgeIcon.startAnimation(pulse)
        badgeIcon.startAnimation(scale)

        // Start Konfetti 🎊
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(
                    0xFFFFC107.toInt(), // Amber
                    0xFF4CAF50.toInt(), // Green
                    0xFF2196F3.toInt(), // Blue
                    0xFFFF4081.toInt()  // Pink
                ),
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(80),
                position = Position.Relative(0.5, 0.3)
            )
        )

        btnClose.setOnClickListener { finish() }
    }
}
