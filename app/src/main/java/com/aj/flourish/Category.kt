package com.aj.flourish

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

// This class defines a database entity (table) named "categories"
@Entity(tableName = "categories")
data class Category(
    // Primary key for the table; autogenerated by Room
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // Name of the category (e.g., "Food", "Transport")
    val imageUri: String, // URI string for the category image (stored as a String for Room compatibility)
    val userId: String // User ID to associate this category with a specific user (for multi-user support)
)