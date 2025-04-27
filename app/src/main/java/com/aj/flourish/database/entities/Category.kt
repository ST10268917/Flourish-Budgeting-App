package com.aj.flourish.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val name: String,
    val imageUri: String? = null
)


