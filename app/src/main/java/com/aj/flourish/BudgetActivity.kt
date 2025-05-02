package com.aj.flourish

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var budgetDao: BudgetDao
    private lateinit var adapter: BudgetAdapter
    private val budgetList = mutableListOf<Budget>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        recyclerView = findViewById(R.id.recyclerViewBudgets)
        fab = findViewById(R.id.btnAddBudget)
        budgetDao = AppDatabase.getInstance(this).budgetDao()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BudgetAdapter(budgetList)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            showAddBudgetDialog()
        }

        loadBudgets()
    }

    private fun loadBudgets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val budgets = budgetDao.getBudgetsForUser(userId)
            withContext(Dispatchers.Main) {
                budgetList.clear()
                budgetList.addAll(budgets)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddBudgetDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_budget, null)
        val etYear = view.findViewById<EditText>(R.id.etYear)
        val etMonth = view.findViewById<EditText>(R.id.etMonth)
        val etMin = view.findViewById<EditText>(R.id.etMin)
        val etMax = view.findViewById<EditText>(R.id.etMax)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setTitle("Set Monthly Budget")
            .setPositiveButton("Save", null) // We override this below
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val yearText = etYear.text.toString().trim()
                val monthText = etMonth.text.toString().trim()
                val minText = etMin.text.toString().trim()
                val maxText = etMax.text.toString().trim()

                val year = yearText.toIntOrNull()
                val month = monthText.toIntOrNull()
                val min = minText.toDoubleOrNull()
                val max = maxText.toDoubleOrNull()

                when {
                    year == null || year !in 1900..2100 -> {
                        Toast.makeText(this, "Enter a valid year (1900–2100)", Toast.LENGTH_SHORT).show()
                    }
                    month == null || month !in 1..12 -> {
                        Toast.makeText(this, "Enter a valid month (1–12)", Toast.LENGTH_SHORT).show()
                    }
                    min == null || max == null -> {
                        Toast.makeText(this, "Enter valid amounts", Toast.LENGTH_SHORT).show()
                    }
                    min > max -> {
                        Toast.makeText(this, "Minimum amount cannot be greater than maximum", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                        val budget = Budget(
                            userId = userId,
                            year = year,
                            month = month,
                            minAmount = min,
                            maxAmount = max
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            budgetDao.insertBudget(budget)
                            withContext(Dispatchers.Main) {
                                loadBudgets()
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        }

        dialog.show()
    }

}
