package com.aj.flourish

import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    suspend fun getExpensesForUser(userId: String): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    suspend fun getExpensesForUserAndCategory(userId: String, categoryId: Int): List<Expense>

    @Delete
    suspend fun deleteExpense(expense: Expense)

}
