package com.aj.flourish

import androidx.room.*

// DAO (Data Access Object) interface for interacting with the 'expenses' table in the Room database
@Dao
interface ExpenseDao {
    // Inserts a new expense or replaces an existing one with the same primary key
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    // Retrieves all expenses for a specific user, ordered by date descending (most recent first)
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    suspend fun getExpensesForUser(userId: String): List<Expense>
    // Retrieves all expenses for a specific user within a specific category, ordered by date descending
    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    suspend fun getExpensesForUserAndCategory(userId: String, categoryId: Int): List<Expense>
    // Deletes the specified expense from the database
    @Delete
    suspend fun deleteExpense(expense: Expense)
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesBetweenDates(userId: String, startDate: Long, endDate: Long): List<Expense>

    @Query("""
    SELECT categories.name AS categoryName, SUM(expenses.amount) AS totalAmount
    FROM expenses
    INNER JOIN categories ON expenses.categoryId = categories.id
    WHERE expenses.userId = :userId AND expenses.date BETWEEN :startDate AND :endDate
    GROUP BY expenses.categoryId
""")
    suspend fun getSpendingPerCategory(userId: String, startDate: Long, endDate: Long): List<CategorySpending>



}
