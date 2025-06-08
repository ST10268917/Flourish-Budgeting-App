package com.aj.flourish

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.R.id.progressBar
import com.aj.flourish.Utils.BadgeConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsActivity : AppCompatActivity() {
    private lateinit var badgeRecyclerView: RecyclerView
    private lateinit var badgeAdapter: BadgeAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val badgeList = BadgeConstants.allBadges
    private val unlockedBadgeIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        badgeRecyclerView = findViewById(R.id.recycler_view_badges)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)

        badgeAdapter = BadgeAdapter(badgeList, unlockedBadgeIds, this) { badge ->
            val intent = Intent(this, BadgeDetailActivity::class.java)
            intent.putExtra("BADGE_ID", badge.id)
            startActivity(intent)
        }

        badgeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        badgeRecyclerView.adapter = badgeAdapter

        loadUnlockedBadges()
    }

    private fun loadUnlockedBadges() {
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        badgeRecyclerView.visibility = View.GONE

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val badgeCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("badges")

        badgeCollection.get()
            .addOnSuccessListener { documents ->
                unlockedBadgeIds.clear()
                for (doc in documents) {
                    val isUnlocked = doc.getBoolean("isUnlocked") ?: false
                    if (isUnlocked) {
                        unlockedBadgeIds.add(doc.id)
                    }
                }

                badgeAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE

                if (unlockedBadgeIds.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                } else {
                    badgeRecyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
                emptyText.text = "Failed to load badges: ${e.message}"
            }
    }
}
