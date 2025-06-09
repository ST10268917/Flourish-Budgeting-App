package com.aj.flourish

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.Utils.BadgeConstants
import com.aj.flourish.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsActivity : BaseActivity() {

    private lateinit var badgeRecyclerView: RecyclerView
    private lateinit var badgeAdapter: BadgeAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        // Initialize views
        badgeRecyclerView = findViewById(R.id.recycler_view_badges)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)

        // Set up RecyclerView and Adapter
        badgeAdapter = BadgeAdapter(BadgeConstants.allBadges.toMutableList(), mutableListOf(), this) { badge ->
            // Handle badge click
            val intent = Intent(this, BadgeDetailActivity::class.java)
            intent.putExtra("BADGE_ID", badge.id)
            startActivity(intent)
        }

        badgeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        badgeRecyclerView.adapter = badgeAdapter

        // Load data
        loadUnlockedBadges()
    }

    private fun loadUnlockedBadges() {
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        badgeRecyclerView.visibility = View.GONE

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            progressBar.visibility = View.GONE
            emptyText.text = "User not logged in."
            emptyText.visibility = View.VISIBLE
            return
        }

        val badgeCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("badges")

        badgeCollection.get()
            .addOnSuccessListener { documents ->
                val unlockedIds = mutableListOf<String>()
                for (doc in documents) {
                    val isUnlocked = doc.getBoolean("isUnlocked") ?: false
                    if (isUnlocked) {
                        unlockedIds.add(doc.id)
                    }
                }

                // Update adapter with new unlocked badges
                badgeAdapter.updateUnlockedBadgeIds(unlockedIds)

                progressBar.visibility = View.GONE
                if (unlockedIds.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                } else {
                    badgeRecyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                emptyText.text = "Failed to load badges: ${e.message}"
                emptyText.visibility = View.VISIBLE
            }
    }
}
