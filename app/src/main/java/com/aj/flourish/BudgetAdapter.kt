package com.aj.flourish

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormatSymbols

class BudgetAdapter(
    private val budgets: List<Budget>,
    private val onEditClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthYear: TextView = itemView.findViewById(R.id.textMonthYear)
        val minMax: TextView = itemView.findViewById(R.id.textMinMax)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        val monthName = DateFormatSymbols().months[budget.month - 1]
        holder.monthYear.text = "$monthName ${budget.year}"

        if (budget.minAmount == 0.0 && budget.maxAmount == 0.0) {
            holder.minMax.text = "No budget set"
            holder.btnEdit.text = "Set"
        } else {
            holder.minMax.text = "Min: ${UserSettings.currencySymbol}${"%.2f".format(budget.minAmount)} | " +
                    "Max: ${UserSettings.currencySymbol}${"%.2f".format(budget.maxAmount)}"
            holder.btnEdit.text = "Edit"
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(budget)
        }
    }

    override fun getItemCount(): Int = budgets.size
}
