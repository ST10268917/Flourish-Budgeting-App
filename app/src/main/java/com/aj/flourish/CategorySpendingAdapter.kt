package com.aj.flourish

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategorySpendingAdapter(private val list: List<CategorySpending>) :
    RecyclerView.Adapter<CategorySpendingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.textCategoryName)
        val amountText: TextView = itemView.findViewById(R.id.textTotalAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_spending, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spending = list[position]
        holder.categoryText.text = spending.categoryName
        holder.amountText.text = "${UserSettings.currencySymbol}${"%.2f".format(spending.totalAmount)}"
    }
}
