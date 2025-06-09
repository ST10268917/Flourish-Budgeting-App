package com.aj.flourish.models

data class CategoryBudget(
    var id: String = "",         // Firestore doc ID or UUID
    var userId: String = "",     // Firebase Auth user ID
    var categoryId: String = "", // Link to Category ID
    var month: Int = 0,          // Month (1-12)
    var year: Int = 0,           // Year (e.g., 2025)
    var budgetAmount: Double = 0.0,
    var minAmount: Double = 0.0, // Minimum budget
    var maxAmount: Double = 0.0// Budget amount for the category
)
