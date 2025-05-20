package com.aj.flourish.repositories

import com.aj.flourish.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class ExpenseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String = auth.currentUser?.uid ?: throw Exception("User not logged in")

    private fun userExpensesRef() =
        firestore.collection("users").document(getUserId()).collection("expenses")

    suspend fun insertExpense(expense: Expense) {
        val newId = expense.id.ifBlank { UUID.randomUUID().toString() }
        val updatedExpense = expense.copy(id = newId, userId = getUserId())
        userExpensesRef().document(newId).set(updatedExpense).await()
    }

    suspend fun getExpensesForCategory(categoryId: String): List<Expense> {
        val snapshot = userExpensesRef().whereEqualTo("categoryId", categoryId).get().await()
        return snapshot.toObjects(Expense::class.java)
    }

    suspend fun getExpensesForUser(): List<Expense> {
        val snapshot = userExpensesRef().get().await()
        return snapshot.toObjects(Expense::class.java)
    }

    suspend fun getExpensesBetweenDates(startDate: Long, endDate: Long): List<Expense> {
        val snapshot = userExpensesRef()
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .await()
        return snapshot.toObjects(Expense::class.java)
    }

    suspend fun deleteExpense(expenseId: String) {
        userExpensesRef().document(expenseId).delete().await()
    }

    suspend fun getExpenseById(expenseId: String): Expense? {
        val doc = userExpensesRef().document(expenseId).get().await()
        return if (doc.exists()) doc.toObject(Expense::class.java) else null
    }
}
