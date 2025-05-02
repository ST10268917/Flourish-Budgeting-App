package com.aj.flourish

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var categoryBtn: Button
    private lateinit var budgetBtn: Button
    private lateinit var allExpensesBtn: Button
    private lateinit var categoriesBtn: Button

    private lateinit var tvWelcome: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var ivSettings: ImageView
    private lateinit var ivNotifications: ImageView
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvOverspentBudget: TextView
    private lateinit var progressBarBudget: ProgressBar
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var recentTransactionsAdapter: ExpenseAdapter
    private val recentTransactionsList = mutableListOf<Expense>()

    private lateinit var budgetDao: BudgetDao
    private lateinit var expenseDao: ExpenseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        budgetDao = AppDatabase.getInstance(this).budgetDao()
        expenseDao = AppDatabase.getInstance(this).expenseDao()

        // Initialize UI elements
        categoryBtn = findViewById(R.id.categoryBtn)
        budgetBtn = findViewById(R.id.budgetBtn)
        allExpensesBtn = findViewById(R.id.allExpensesBtn)
        categoriesBtn = findViewById(R.id.categoriesBtn)

        tvWelcome = findViewById(R.id.tvWelcome)
        ivProfile = findViewById(R.id.ivProfile)
        ivSettings = findViewById(R.id.ivSettings)
        ivNotifications = findViewById(R.id.ivNotifications)
        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
        tvOverspentBudget = findViewById(R.id.tvOverspentBudget)
        progressBarBudget = findViewById(R.id.progressBarBudget)
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)

        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        recentTransactionsAdapter = ExpenseAdapter(recentTransactionsList) {}
        rvRecentTransactions.adapter = recentTransactionsAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "User"
                    val currency = document.getString("currency") ?: "ZAR"

                    UserSettings.currency = currency
                    tvWelcome.text = "Welcome, $username!"

                    updateBudgetDisplay()
                    loadRecentTransactions(userId)
                    loadCurrentBudget(userId) // Call loadCurrentBudget here

                    // Icon click listeners (placeholders - implement later if needed)
                    ivProfile.setOnClickListener {
                        Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                        // startActivity(Intent(this, ProfileActivity::class.java))
                    }

                    ivSettings.setOnClickListener {
                        Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                        // startActivity(Intent(this, SettingsActivity::class.java))
                    }

                    ivNotifications.setOnClickListener {
                        Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
                        // startActivity(Intent(this, NotificationsActivity::class.java))
                    }

                } else {
                    updateBudgetDisplay()
                    loadRecentTransactions(userId)
                    loadCurrentBudget(userId) // Call loadCurrentBudget here as well
                }
            }
            .addOnFailureListener {
                updateBudgetDisplay()
                loadRecentTransactions(userId)
                loadCurrentBudget(userId) // Call loadCurrentBudget here in case of failure
            }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Keep original navigation,
        categoryBtn.setOnClickListener {
            startActivity(Intent(this, CreateCategory::class.java))

        }

        budgetBtn.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))

        }

        allExpensesBtn.setOnClickListener {
            startActivity(Intent(this, FilterExpensesActivity::class.java))

        }

        categoriesBtn.setOnClickListener {
            startActivity(Intent(this, CategorySpendingActivity::class.java))

        }

        // Set icons for buttons
        setButtonIcons()
    }

    private fun setButtonIcons() {
        val paddingInPixels = resources.getDimensionPixelSize(R.dimen.button_icon_padding) // Define this in dimens.xml
        categoryBtn.compoundDrawablePadding = paddingInPixels
        budgetBtn.compoundDrawablePadding = paddingInPixels
        allExpensesBtn.compoundDrawablePadding = paddingInPixels
        categoriesBtn.compoundDrawablePadding = paddingInPixels

        categoryBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_category, 0, 0, 0)
        budgetBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_budget, 0, 0, 0)
        allExpensesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expenses, 0, 0, 0)
        categoriesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_categories, 0, 0, 0)
    }

    private fun loadCurrentBudget(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Month is 0-indexed

            // Get all budgets for the current year
            val budgetsThisYear = budgetDao.getBudgetsForUserAndYear(userId, currentYear)

            // Find the budget for the current month
            val currentBudget = budgetsThisYear.find { it.month == currentMonth }

            // Get total expenses for the current month
            val totalExpenses = expenseDao.getTotalExpensesForMonthAndYear(userId, currentMonth, currentYear)

            withContext(Dispatchers.Main) {
                if (currentBudget != null) {
                    val maxBudget = currentBudget.maxAmount
                    val spentAmount = totalExpenses ?: 0.0
                    val remaining = maxBudget - spentAmount
                    val overspent = if (remaining < 0) -remaining else 0.0

                    tvTotalBudget.text = "${UserSettings.currencySymbol} ${String.format("%.2f", maxBudget)}"
                    tvRemainingBudget.text = "${UserSettings.currencySymbol} ${String.format("%.2f", if (remaining > 0) remaining else 0.0)}"
                    tvOverspentBudget.text = "${UserSettings.currencySymbol} ${String.format("%.2f", overspent)}"

                    progressBarBudget.max = maxBudget.toInt()
                    progressBarBudget.progress = spentAmount.toInt()
                } else {
                    // No budget set for this month
                    tvTotalBudget.text = "${UserSettings.currencySymbol} 0.00"
                    tvRemainingBudget.text = "${UserSettings.currencySymbol} 0.00"
                    tvOverspentBudget.text = "${UserSettings.currencySymbol} 0.00"
                    progressBarBudget.max = 100
                    progressBarBudget.progress = 0
                }
            }
        }
    }

    private fun loadRecentTransactions(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val recentExpenses = expenseDao.getExpensesForUser(userId).take(5)
            withContext(Dispatchers.Main) {
                recentTransactionsList.clear()
                recentTransactionsList.addAll(recentExpenses)
                recentTransactionsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateBudgetDisplay() {
        tvTotalBudget.text = tvTotalBudget.text.toString().replace(Regex("^[A-Za-z$€£R ]+"), "${UserSettings.currencySymbol} ")
        tvRemainingBudget.text = tvRemainingBudget.text.toString().replace(Regex("^[A-Za-z$€£R ]+"), "${UserSettings.currencySymbol} ")
        tvOverspentBudget.text = tvOverspentBudget.text.toString().replace(Regex("^[A-Za-z$€£R ]+"), "${UserSettings.currencySymbol} ")
    }
}