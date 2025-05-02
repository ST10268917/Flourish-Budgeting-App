package com.aj.flourish

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormatSymbols

class BudgetAdapter(private val budgets: List<Budget>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {
    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthYear: TextView = itemView.findViewById(R.id.textMonthYear)
        val minMax: TextView = itemView.findViewById(R.id.textMinMax)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        val monthName = DateFormatSymbols().months[budget.month - 1]
        holder.monthYear.text = "$monthName ${budget.year}"
        holder.minMax.text = "Min: ${UserSettings.currency}${budget.minAmount} | Max: ${UserSettings.currency}${budget.maxAmount}"
    }

    override fun getItemCount(): Int = budgets.size
}
