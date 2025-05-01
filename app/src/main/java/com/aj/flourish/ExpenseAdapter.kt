package com.aj.flourish

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewExpenseDate)
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewExpenseAmount)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewExpenseDescription)
        val receiptImageView: ImageView = itemView.findViewById(R.id.receiptImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.textViewDate.text = formatDate(expense.date)
        holder.textViewAmount.text = "${UserSettings.currencySymbol} ${expense.amount}0"
        holder.textViewDescription.text = expense.description

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is CategoryDetail) {
                context.showExpenseDetailDialog(expense)
            }
        }
        // Handle the receipt image
        if (!expense.receiptUri.isNullOrEmpty()) {
            try {
                Glide.with(holder.itemView.context)
                    .load(expense.receiptUri)
                    .placeholder(R.drawable.ic_receipt_placeholder) // Add a placeholder drawable
                    .error(R.drawable.ic_error) // Add an error drawable
                    .into(holder.receiptImageView)
                holder.receiptImageView.visibility = View.VISIBLE
            } catch (e: Exception) {
                holder.receiptImageView.visibility = View.GONE
                Log.e("ExpenseAdapter", "Error loading image: ${e.message}")
            }
        } else {
            holder.receiptImageView.visibility = View.GONE
        }
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
