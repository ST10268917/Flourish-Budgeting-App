package com.aj.flourish

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        holder.imageViewCategory.setImageURI(Uri.parse(category.imageUri))

        // Item click listener
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CategoryDetail::class.java)
            intent.putExtra("categoryName", category.name)
            intent.putExtra("categoryImageUri", category.imageUri.toString())
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = categories.size
}
