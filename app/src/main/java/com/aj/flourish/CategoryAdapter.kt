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
import com.aj.flourish.models.Category
import com.bumptech.glide.Glide

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

        // Load image from Supabase URL using Glide
        if (!category.imageUri.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(category.imageUri)
                .placeholder(R.drawable.ic_receipt_placeholder) // Optional: show while loading
                .error(R.drawable.ic_error) // Optional: show on failure
                .into(holder.imageViewCategory)
        } else {
            holder.imageViewCategory.setImageResource(R.drawable.ic_receipt_placeholder)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (category.name.isNullOrBlank()) {
                Toast.makeText(context, "Category name is missing!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(context, CategoryDetail::class.java).apply {
                putExtra("categoryName", category.name)
                putExtra("categoryId", category.id)
                putExtra("categoryImageUri", category.imageUri ?: "")
            }
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
