package com.aj.flourish

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.aj.flourish.models.Expense
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

// Adapter for displaying a list of Expense items in a RecyclerView
class ExpenseAdapter(private val expenses: List<Expense>, private val onItemClick: (Expense) -> Unit) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    // ViewHolder holds references to the UI elements for each expense item
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDate: TextView = itemView.findViewById(R.id.textViewExpenseDate) // Displays the date of the expense
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewExpenseAmount)  // Displays the amount spent
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewExpenseDescription) // Displays a description of the expense
        val receiptImageView: ImageView = itemView.findViewById(R.id.receiptImageView) // Displays the optional receipt image
    }
    // Inflate the layout for each item and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }
    // Binds each expense item to the UI elements in the ViewHolder
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.textViewDate.text = formatDate(expense.date)
        holder.textViewAmount.text = "${UserSettings.currencySymbol} ${"%.2f".format(expense.amount)}"
        holder.textViewDescription.text = expense.description

        // Receipt image handling
        if (!expense.receiptUri.isNullOrEmpty()) {
            try {
                Glide.with(holder.itemView.context)
                    .load(expense.receiptUri)
                    .placeholder(R.drawable.ic_receipt_placeholder)
                    .error(R.drawable.ic_error)
                    .into(holder.receiptImageView)
                holder.receiptImageView.visibility = View.VISIBLE
            } catch (e: Exception) {
                holder.receiptImageView.visibility = View.GONE
                Log.e("ExpenseAdapter", "Error loading image: ${e.message}")
            }
        } else {
            holder.receiptImageView.visibility = View.GONE
        }

        // Use provided click listener
        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }



    // Returns the total number of expense items
    override fun getItemCount(): Int = expenses.size

    // Helper function to convert a timestamp into a readable date format
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
