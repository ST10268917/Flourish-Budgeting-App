package com.aj.flourish

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val categoryId: Int,
    val description: String,
    val amount: Double,
    val date: Long,
    val receiptUri: String? = null
)
