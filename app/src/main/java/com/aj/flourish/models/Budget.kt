package com.aj.flourish.models

data class Budget(
    var id: String = "",         // Firestore-generated or UUID
    var userId: String = "",     // Firebase Auth user ID
    var month: Int = 0,          // Month (1-12)
    var year: Int = 0,           // Year (e.g., 2025)
    var minAmount: Double = 0.0, // Minimum budget
    var maxAmount: Double = 0.0  // Maximum budget
)
