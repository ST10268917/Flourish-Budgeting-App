package com.aj.flourish.repositories

import com.aj.flourish.models.Budget
import com.aj.flourish.models.CategoryBudget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class BudgetRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String = auth.currentUser?.uid ?: throw Exception("User not logged in")

    private fun userBudgetsRef() =
        firestore.collection("users").document(getUserId()).collection("budgets")

    suspend fun insertOrUpdateBudget(budget: Budget) {
        val docId = "${budget.year}_${budget.month}" // Unique doc ID per month/year
        val updatedBudget = budget.copy(userId = getUserId())
        userBudgetsRef().document(docId).set(updatedBudget).await()
    }

    suspend fun getBudgetsForYear(year: Int): List<Budget> {
        val snapshot = userBudgetsRef()
            .whereEqualTo("year", year)
            .get().await()
        return snapshot.toObjects(Budget::class.java)
    }

    suspend fun getBudgetsForUser(): List<Budget> {
        val snapshot = userBudgetsRef().get().await()
        return snapshot.toObjects(Budget::class.java)
    }

    suspend fun getCategoryBudgetsForMonth(year: Int, month: Int): List<CategoryBudget> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val budgets = mutableListOf<CategoryBudget>()
        val snapshot = FirebaseFirestore.getInstance()
            .collection("category_budgets")
            .whereEqualTo("userId", userId)
            .whereEqualTo("year", year)
            .whereEqualTo("month", month)
            .get()
            .await()

        for (doc in snapshot.documents) {
            budgets.add(doc.toObject(CategoryBudget::class.java)!!.copy(id = doc.id))
        }
        return budgets
    }

}
