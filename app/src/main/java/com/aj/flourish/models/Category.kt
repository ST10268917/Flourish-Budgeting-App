package com.aj.flourish.models

data class Category (
    var id: String = "",               // Firestore-generated for the category
    var name: String = "",            // Name of the category
    var imageUri: String = "",        // Image URI stored as a String
    var userId: String = ""
)