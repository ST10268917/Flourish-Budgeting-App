package com.aj.flourish

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewExpenseDate)
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewExpenseAmount)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewExpenseDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.textViewDate.text = formatDate(expense.date)
        holder.textViewAmount.text = "Amount: ${expense.amount}"
        holder.textViewDescription.text = expense.description

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is CategoryDetail) {
                context.showExpenseDetailDialog(expense)
            }
        }
    }


    override fun getItemCount(): Int = expenses.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
