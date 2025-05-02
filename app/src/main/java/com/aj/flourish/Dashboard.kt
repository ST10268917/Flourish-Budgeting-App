package com.aj.flourish

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Dashboard : AppCompatActivity() {

    private lateinit var categoryBtn: Button
    private lateinit var budgetBtn: Button
    private lateinit var allExpensesBtn: Button
    private lateinit var categoriesBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currency = document.getString("currency")
                    // Store in a singleton or SharedPreferences for later use
                    UserSettings.currency = currency ?: "ZAR"
                }
            }

        categoryBtn = findViewById(R.id.categoryBtn)
        budgetBtn = findViewById(R.id.budgetBtn)
        allExpensesBtn = findViewById(R.id.allExpensesBtn)
        categoriesBtn = findViewById(R.id.categoriesBtn)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        categoryBtn.setOnClickListener {
            startActivity(Intent(this, CreateCategory::class.java))
            finish()
        }

        budgetBtn.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
            finish()
        }

        allExpensesBtn.setOnClickListener {
            startActivity(Intent(this, FilterExpensesActivity::class.java))
            finish()
        }
        categoriesBtn.setOnClickListener {
            startActivity(Intent(this, CategorySpendingActivity::class.java))
            finish()
        }

    }
}