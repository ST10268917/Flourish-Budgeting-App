package com.aj.flourish.repositories

import com.aj.flourish.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class CategoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String = auth.currentUser?.uid ?: throw Exception("User not logged in")

    private fun userCategoriesRef() =
        firestore.collection("users").document(getUserId()).collection("categories")

    // Insert or update category
    suspend fun insertCategory(category: Category) {
        val newId = category.id.ifBlank { UUID.randomUUID().toString() }
        val updatedCategory = category.copy(id = newId, userId = getUserId())

        userCategoriesRef().document(newId).set(updatedCategory).await()
    }

    // Get all categories for current user
    suspend fun getCategories(): List<Category> {
        val snapshot = userCategoriesRef().get().await()
        return snapshot.toObjects(Category::class.java)
    }

    // Delete category
    suspend fun deleteCategory(categoryId: String) {
        userCategoriesRef().document(categoryId).delete().await()
    }

    // Get a single category by ID
    suspend fun getCategoryById(categoryId: String): Category? {
        val doc = userCategoriesRef().document(categoryId).get().await()
        return if (doc.exists()) doc.toObject(Category::class.java) else null
    }
}
