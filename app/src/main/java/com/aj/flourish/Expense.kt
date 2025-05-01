package com.aj.flourish

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Define this data class as a Room Entity representing a table named "expenses"
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,   // Link to the Category entity
            parentColumns = ["id"],     // Parent column in the Category table
            childColumns = ["categoryId"],    // Corresponding column in this table
            onDelete = ForeignKey.CASCADE      // If a Category is deleted, delete related expenses
        )
    ]
)

data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-incrementing primary key
    val userId: String,     // ID of the user who made the expense (from Firebase)
    val categoryId: Int,      // Foreign key linking to the category of the expense
    val description: String,    // Short description of the expense
    val amount: Double,         // Amount spent
    val date: Long,               // Timestamp representing the date of the expense
    val receiptUri: String? = null  // Optional URI to a receipt image stored internally
)
