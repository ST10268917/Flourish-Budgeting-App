package com.aj.flourish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.Utils.BadgeManager
import com.aj.flourish.models.Budget
import com.aj.flourish.repositories.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.text.DateFormatSymbols
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BudgetAdapter
    private val budgetList = mutableListOf<Budget>()
    private lateinit var backButton: ImageView

    private lateinit var tvCurrentYear: TextView
    private lateinit var btnPrevYear: TextView
    private lateinit var btnNextYear: TextView

    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        recyclerView = findViewById(R.id.recyclerViewBudgets)
        tvCurrentYear = findViewById(R.id.tvCurrentYear)
        btnPrevYear = findViewById(R.id.btnPrevYear)
        btnNextYear = findViewById(R.id.btnNextYear)
        backButton = findViewById(R.id.ivBack)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BudgetAdapter(budgetList) { budget ->
            showAddBudgetDialog(budget)
        }
        recyclerView.adapter = adapter

        tvCurrentYear.text = currentYear.toString()

        btnPrevYear.setOnClickListener {
            currentYear--
            tvCurrentYear.text = currentYear.toString()
            loadBudgets()
        }

        btnNextYear.setOnClickListener {
            currentYear++
            tvCurrentYear.text = currentYear.toString()
            loadBudgets()
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        loadBudgets()
    }

    private fun loadBudgets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val existing = BudgetRepository().getBudgetsForYear(currentYear)
            val existingMap = existing.associateBy { it.month }

            val fullList = (1..12).map { month ->
                existingMap[month] ?: Budget(
                    userId = userId,
                    year = currentYear,
                    month = month,
                    minAmount = 0.0,
                    maxAmount = 0.0
                )
            }

            withContext(Dispatchers.Main) {
                budgetList.clear()
                budgetList.addAll(fullList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddBudgetDialog(budget: Budget) {
        val view = layoutInflater.inflate(R.layout.dialog_add_budget, null)
        val etMin = view.findViewById<EditText>(R.id.etMin)
        val etMax = view.findViewById<EditText>(R.id.etMax)

        etMin.setText(if (budget.minAmount > 0) budget.minAmount.toString() else "")
        etMax.setText(if (budget.maxAmount > 0) budget.maxAmount.toString() else "")

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setTitle("Budget for ${DateFormatSymbols().months[budget.month - 1]} ${budget.year}")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                Log.d("BudgetActivity", "Save button clicked!")
                val min = etMin.text.toString().toDoubleOrNull()
                val max = etMax.text.toString().toDoubleOrNull()

                if (min == null || max == null || min > max) {
                    Toast.makeText(this, "Enter valid amounts", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedBudget = budget.copy(
                    minAmount = min,
                    maxAmount = max
                )
                val prefs = getSharedPreferences("user_progress", Context.MODE_PRIVATE)
                val isFirstBudget = !prefs.getBoolean("first_budget_created", false)

                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("BudgetActivity", "Inside CoroutineScope - about to save budget")
                    BudgetRepository().insertOrUpdateBudget(updatedBudget)
                    withContext(Dispatchers.Main) {
                        loadBudgets()
                        dialog.dismiss()
                    }
                    if (isFirstBudget) {
                        BadgeManager.checkAndUnlockBadge(this@BudgetActivity, "first_budget")
                        prefs.edit().putBoolean("first_budget_created", true).apply()
                    }
                }
            }
        }

        dialog.show()


    }
    }

