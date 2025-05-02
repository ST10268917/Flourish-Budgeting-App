package com.aj.flourish

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val month: Int,  // 1-12
    val year: Int,
    val minAmount: Double,
    val maxAmount: Double
)