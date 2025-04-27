package com.aj.flourish.database.dao

import androidx.room.*
import com.aj.flourish.database.entities.Category

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: String): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)

}
