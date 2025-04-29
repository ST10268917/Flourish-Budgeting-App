package com.aj.flourish

import androidx.room.*

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: String): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)

}
