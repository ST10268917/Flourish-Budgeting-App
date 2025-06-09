package com.aj.flourish

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.Utils.BadgeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryBudgetAdapter(private val budgets: List<CategoryBudgetDisplay>)
    : RecyclerView.Adapter<CategoryBudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvBudgetAmount: TextView = itemView.findViewById(R.id.tvBudgetAmount)
        val tvSpentAmount: TextView = itemView.findViewById(R.id.tvSpentAmount)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val item = budgets[position]
        holder.tvCategoryName.text = item.categoryName
        holder.tvBudgetAmount.text = "Budget: ${UserSettings.currencySymbol}${"%.2f".format(item.budgetAmount)}"
        holder.tvSpentAmount.text = "Spent: ${UserSettings.currencySymbol}${"%.2f".format(item.spentAmount)}"

        val progress = if (item.budgetAmount > 0) {
            (item.spentAmount / item.budgetAmount * 100).toInt().coerceAtMost(100)
        } else {
            0
        }

        // Animate progress bar
        ObjectAnimator.ofInt(holder.progressBar, "progress", 0, progress).apply {
            duration = 800
            start()
        }

        // Award badge for staying under budget
        if (item.spentAmount <= item.budgetAmount && item.spentAmount > 0) {
            CoroutineScope(Dispatchers.Main).launch {
                BadgeManager.checkAndUnlockBadge(holder.itemView.context, "under_budget")
            }
        }
    }

    override fun getItemCount(): Int = budgets.size
}
