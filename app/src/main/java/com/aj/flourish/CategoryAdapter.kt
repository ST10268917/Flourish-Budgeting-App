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

class CategoryAdapter(private val categories: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCategory: ImageView = itemView.findViewById(R.id.imageViewCategory)
        val textViewCategoryName: TextView = itemView.findViewById(R.id.textViewCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textViewCategoryName.text = category.name

        // 🛡 Error handling for image URI
        try {
            val uri = Uri.parse(category.imageUri)
            if (uri != null && category.imageUri.isNotBlank()) {
                holder.imageViewCategory.setImageURI(uri)
            } else {
                holder.imageViewCategory.setImageResource(R.drawable.ic_receipt_placeholder) // fallback image
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.imageViewCategory.setImageResource(R.drawable.ic_error) // fallback on error
        }

        // 🚦 Null safety and user-friendly navigation
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


    override fun getItemCount(): Int = categories.size
}
