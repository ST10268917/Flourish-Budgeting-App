package com.aj.flourish

import androidx.room.*

// Data Access Object (DAO) for the Category entity
@Dao
interface CategoryDao {

    // Inserts a new category into the database
    @Insert
    suspend fun insertCategory(category: Category)
    // Retrieves all categories for a specific user by userId
    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: String): List<Category>
    // Deletes a specific category from the database
    @Delete
    suspend fun deleteCategory(category: Category)

}
