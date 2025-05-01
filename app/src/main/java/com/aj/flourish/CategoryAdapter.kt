package com.aj.flourish

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

// Adapter class to display a list of Category items in a RecyclerView
class CategoryAdapter(private val categories: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // ViewHolder holds references to the views for each item in the RecyclerView
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCategory: ImageView = itemView.findViewById(R.id.imageViewCategory)
        val textViewCategoryName: TextView = itemView.findViewById(R.id.textViewCategoryName)
    }
    // Inflates the layout for each RecyclerView item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }
    // Binds the data from each Category object to its corresponding view in the ViewHolder
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textViewCategoryName.text = category.name

        // Try to load the category image from its URI, with fallback in case of error
        try {
            val uri = Uri.parse(category.imageUri)
            if (uri != null && category.imageUri.isNotBlank()) {
                holder.imageViewCategory.setImageURI(uri)
            } else {
                // Fallback image if URI is blank
                holder.imageViewCategory.setImageResource(R.drawable.ic_receipt_placeholder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.imageViewCategory.setImageResource(R.drawable.ic_error) // fallback on error
        }

        // Set click listener to open category details screen when an item is tapped
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // Check for a valid category name before proceeding
            if (category.name.isNullOrBlank()) {
                Toast.makeText(context, "Category name is missing!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Create intent and attach category data to send to CategoryDetail activity
            val intent = Intent(context, CategoryDetail::class.java).apply {
                putExtra("categoryName", category.name)
                putExtra("categoryId", category.id)
                putExtra("categoryImageUri", category.imageUri ?: "")
            }
              // Attempt to start the activity safely
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open category details.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Returns the total number of items to be displayed in the RecyclerView
    override fun getItemCount(): Int = categories.size
}
