package com.aj.flourish

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.models.Badge

class BadgeAdapter(
    private val badgeList: List<Badge>,
    private val unlockedBadgeIds: MutableList<String> = mutableListOf(),
    private val context: Context,
    private val onBadgeClick: ((Badge) -> Unit)? = null
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badgeList[position]
        val isUnlocked = unlockedBadgeIds.contains(badge.id)

        holder.badgeName.text = badge.name
        val iconRes = badge.iconRes.takeIf { it != 0 } ?: R.drawable.ic_badge_default
        holder.badgeIcon.setImageResource(iconRes)

        // Set visuals based on unlock status
        if (isUnlocked) {
            holder.badgeContainer.setBackgroundResource(R.drawable.glow_badge_background)
            holder.badgeIcon.clearColorFilter()
            holder.badgeIcon.alpha = 1f
        } else {
            holder.badgeContainer.setBackgroundResource(R.drawable.locked_badge_background)
            holder.badgeIcon.setColorFilter(
                ContextCompat.getColor(context, R.color.grey_dark),
                PorterDuff.Mode.SRC_IN
            )
            holder.badgeIcon.alpha = 0.5f
        }

        // Bounce animation
        holder.badgeContainer.scaleX = 0f
        holder.badgeContainer.scaleY = 0f
        holder.badgeContainer.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .start()

        // Click action
        holder.itemView.setOnClickListener {
            if (isUnlocked) {
                onBadgeClick?.invoke(badge)
                    ?: Toast.makeText(context, "${badge.name}: ${badge.description}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "This badge is still locked!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = badgeList.size

    fun updateUnlockedBadgeIds(newUnlockedIds: List<String>) {
        unlockedBadgeIds.clear()
        unlockedBadgeIds.addAll(newUnlockedIds)
        notifyDataSetChanged()
    }

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val badgeIcon: ImageView = itemView.findViewById(R.id.badge_icon)
        val badgeName: TextView = itemView.findViewById(R.id.badge_name)
        val badgeContainer: View = itemView.findViewById(R.id.badgeContainer)
    }
}
