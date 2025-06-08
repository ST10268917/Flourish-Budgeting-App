package com.aj.flourish

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aj.flourish.Utils.BadgeConstants
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class BadgePopupActivity : AppCompatActivity() {

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
            badgeTitle.text = "🎉 Badge Unlocked!"
            badgeDescription.text = "${badge.name}\n\n${badge.description}"
        } else {
            badgeIcon.setImageResource(R.drawable.ic_badge_default)
            badgeTitle.text = "Badge Unlocked!"
            badgeDescription.text = "You've earned a badge!"
        }

        // Confetti celebration 🎊
        konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xFFF44336.toInt(), 0xFF4CAF50.toInt(), 0xFF2196F3.toInt()),
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(80),
                position = Position.Relative(0.5, 0.3)
            )
        )

        btnClose.setOnClickListener { finish() }
    }
}
