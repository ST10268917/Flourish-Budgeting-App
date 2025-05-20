package com.aj.flourish.models

data class Expense(
    var id: String = "", // Firebase document ID
    var userId: String = "", // User who added the expense
    var categoryId: String = "", // Firestore category document ID
    var description: String = "",
    var amount: Double = 0.0,
    var date: Long = 0L,
    var receiptUri: String? = null
)
