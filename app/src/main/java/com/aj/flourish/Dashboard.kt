package com.aj.flourish

// Standard AndroidX and Android UI imports
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Spinner // New import for Spinner
import android.widget.AdapterView // New import for AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter // New import for ArrayAdapter if needed

// Coroutines imports for asynchronous operations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Java Utility imports
import java.util.Calendar
import android.util.Log // New import for Log.d

// Firebase Authentication import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Your app's Repository imports (ensure these paths are correct for your project)
import com.aj.flourish.repositories.ExpenseRepository
import com.aj.flourish.repositories.CategoryRepository
import com.aj.flourish.repositories.BudgetRepository

// Your app's Model imports (ensure these paths are correct for your project)
import com.aj.flourish.models.Expense
import com.aj.flourish.models.Category
import com.aj.flourish.models.Budget

// MPAndroidChart imports
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.Legend
import android.graphics.Color
import android.view.View // New import for android.view.View
import com.aj.flourish.base.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.tasks.await
import com.aj.flourish.R


class Dashboard : BaseActivity() {

    // Existing Button declarations
    private lateinit var categoryBtn: Button
    private lateinit var budgetBtn: Button
    private lateinit var allExpensesBtn: Button
    private lateinit var categoriesBtn: Button
    private lateinit var tvLoginStreak: TextView
    private lateinit var currencyConverterBtn: Button

    private lateinit var rvCategoryBudgets: RecyclerView
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter
    private val categoryBudgetList = mutableListOf<CategoryBudgetDisplay>()

    // Existing Header UI declarations
    private lateinit var tvWelcome: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var ivSettings: ImageView
    private lateinit var ivNotifications: ImageView

    // Existing Budget Overview CardView UI declarations (ProgressBar removed)
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvOverspentBudget: TextView

    // New UI elements for overall monthly spending summary and chart
    private lateinit var tvTotalSpending: TextView
    private lateinit var tvMonthlyBudget: TextView
    private lateinit var categorySpendingChart: BarChart

    private lateinit var spinnerPeriodFilter: Spinner // Declaration for the new Spinner

    // Recent Transactions RecyclerView declarations
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var recentTransactionsAdapter: ExpenseAdapter
    private val recentTransactionsList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)


        // Initialize ALL UI elements from activity_dashboard.xml
        categoryBtn = findViewById(R.id.categoryBtn)
        budgetBtn = findViewById(R.id.budgetBtn)
        allExpensesBtn = findViewById(R.id.allExpensesBtn)
        categoriesBtn = findViewById(R.id.categoriesBtn)
        tvLoginStreak = findViewById(R.id.tvLoginStreak)
        currencyConverterBtn = findViewById(R.id.currencyConverterBtn)

        rvCategoryBudgets = findViewById(R.id.rvCategoryBudgets)
        rvCategoryBudgets.layoutManager = LinearLayoutManager(this)
        categoryBudgetAdapter = CategoryBudgetAdapter(categoryBudgetList)
        rvCategoryBudgets.adapter = categoryBudgetAdapter


        tvWelcome = findViewById(R.id.tvWelcome)
        ivProfile = findViewById(R.id.ivProfile)
        ivSettings = findViewById(R.id.ivSettings)
        ivNotifications = findViewById(R.id.ivNotifications)

        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
        tvOverspentBudget = findViewById(R.id.tvOverspentBudget)

        // --- NEW CHART AND SUMMARY UI INITIALIZATIONS ---
        tvTotalSpending = findViewById(R.id.tvTotalSpending)
        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget)
        categorySpendingChart = findViewById(R.id.categorySpendingChart)
        spinnerPeriodFilter = findViewById(R.id.spinnerPeriodFilter) // Initialize the Spinner
        // --- END NEW CHART UI INITIALIZATIONS ---

        rvRecentTransactions = findViewById(R.id.rvRecentTransactions)
        rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        recentTransactionsAdapter = ExpenseAdapter(recentTransactionsList) {}
        rvRecentTransactions.adapter = recentTransactionsAdapter

        val calculatorBtn = findViewById<Button>(R.id.calculatorBtn)
        val achievementsBtn = findViewById<Button>(R.id.achievementsBtn)

        val btnViewAllBadges = findViewById<Button>(R.id.btnViewAllBadges)
        btnViewAllBadges.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java)) // Replace with BadgesActivity if you used that name
        }

        // Set up the Spinner listener BEFORE loading initial data
        spinnerPeriodFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // When a new item is selected, recalculate and load data
                Log.d("Dashboard", "Spinner item selected: Position $position, ID $id")
                loadDashboardDataForSelectedPeriod(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
                Log.d("Dashboard", "Spinner: Nothing selected.")
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_LONG).show()
            finish() // End activity if no user
            return
        }

        // Fetch user data first to set currency, then load dashboard data for the initially selected period
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "User"
                    val currency = document.getString("currency") ?: "ZAR"

                    UserSettings.currency = currency
                    tvWelcome.text = "Welcome, $username!"
                } else {
                    UserSettings.currency = "ZAR"
                    tvWelcome.text = "Welcome, User!"
                }
                // Call the new method to load data for the initially selected period (default: This Month)
                // This will trigger the spinner's onItemSelected and load data
                loadDashboardDataForSelectedPeriod(spinnerPeriodFilter.selectedItemPosition)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load user settings: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Dashboard", "Error loading user settings", e)
                UserSettings.currency = "ZAR" // Fallback to default currency
                tvWelcome.text = "Welcome, User!"
                loadDashboardDataForSelectedPeriod(spinnerPeriodFilter.selectedItemPosition) // Load data even if user settings fail
            }

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up click listeners for the main action buttons
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


        currencyConverterBtn.setOnClickListener {
            startActivity(Intent(this, CurrencyConverterActivity::class.java))
        }
        calculatorBtn.setOnClickListener {
            startActivity(Intent(this, CalculatorActivity::class.java))
        }

        achievementsBtn.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        setButtonIcons() // Your existing function to set drawable icons on buttons

        val prefs = getSharedPreferences("login_tracker", Context.MODE_PRIVATE)
        val streak = prefs.getInt("login_streak", 0)
        tvLoginStreak.text = "Login Streak: $streak day${if (streak != 1) "s" else ""}"


    }

    // Your existing function to set button icons
    private fun setButtonIcons() {
        val paddingInPixels = resources.getDimensionPixelSize(R.dimen.button_icon_padding)
        categoryBtn.compoundDrawablePadding = paddingInPixels
        budgetBtn.compoundDrawablePadding = paddingInPixels
        allExpensesBtn.compoundDrawablePadding = paddingInPixels
        categoriesBtn.compoundDrawablePadding = paddingInPixels

        categoryBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_category, 0, 0, 0)
        budgetBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_budget, 0, 0, 0)
        allExpensesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expenses, 0, 0, 0)
        categoriesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_categories, 0, 0, 0)
    }

    // Helper function to determine dates based on Spinner selection ---
    private fun loadDashboardDataForSelectedPeriod(periodIndex: Int) {
        val calendar = Calendar.getInstance()
        var startDate: Long
        var endDate: Long
        var isCurrentMonthSelected = false

        when (periodIndex) {
            0 -> { // This Month
                isCurrentMonthSelected = true
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                endDate = calendar.timeInMillis
                Log.d("Dashboard", "Period: This Month, Start: ${Calendar.getInstance().apply { timeInMillis = startDate }.time}, End: ${Calendar.getInstance().apply { timeInMillis = endDate }.time}")
            }
            1 -> { // Last Month
                calendar.add(Calendar.MONTH, -1) // Go back one month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1) // Go to the first day of current month
                calendar.add(Calendar.MILLISECOND, -1) // Go back to last millisecond of last month
                endDate = calendar.timeInMillis
                Log.d("Dashboard", "Period: Last Month, Start: ${Calendar.getInstance().apply { timeInMillis = startDate }.time}, End: ${Calendar.getInstance().apply { timeInMillis = endDate }.time}")
            }
            2 -> { // Last 3 Months
                calendar.add(Calendar.MONTH, -2) // Go back two months (current month is the third)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                val currentMonthCal = Calendar.getInstance()
                currentMonthCal.add(Calendar.MONTH, 1)
                currentMonthCal.add(Calendar.MILLISECOND, -1)
                endDate = currentMonthCal.timeInMillis
                Log.d("Dashboard", "Period: Last 3 Months, Start: ${Calendar.getInstance().apply { timeInMillis = startDate }.time}, End: ${Calendar.getInstance().apply { timeInMillis = endDate }.time}")
            }
            3 -> { // This Year
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                endDate = calendar.timeInMillis
                Log.d("Dashboard", "Period: This Year, Start: ${Calendar.getInstance().apply { timeInMillis = startDate }.time}, End: ${Calendar.getInstance().apply { timeInMillis = endDate }.time}")
            }
            4 -> { // All Time
                calendar.set(1900, Calendar.JANUARY, 1, 0, 0, 0) // Very old date as start
                startDate = calendar.timeInMillis
                endDate = Long.MAX_VALUE // Represents "until now" or "infinity" for expense fetching
                Log.d("Dashboard", "Period: All Time, Start: ${Calendar.getInstance().apply { timeInMillis = startDate }.time}, End: Max Value")
            }
            else -> { // Default to This Month if somehow invalid index
                isCurrentMonthSelected = true
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                endDate = calendar.timeInMillis
                Log.w("Dashboard", "Invalid period index: $periodIndex. Defaulting to This Month.")
            }
        }
        // Call the main data loading function with the calculated dates and current month flag
        loadDashboardData(startDate, endDate, isCurrentMonthSelected)
    }


    // loadDashboardData accepts start and end dates, and an isCurrentMonth flag ---
    private fun loadDashboardData(startDate: Long, endDate: Long, isCurrentMonth: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("Dashboard", "loadDashboardData executing with Start: $startDate, End: $endDate, IsCurrentMonth: $isCurrentMonth")

        CoroutineScope(Dispatchers.IO).launch {
            val expensesForSelectedPeriod = ExpenseRepository().getExpensesBetweenDates(startDate, endDate)
            Log.d("Dashboard", "Fetched ${expensesForSelectedPeriod.size} expenses for the selected period.")

            val allCategories = CategoryRepository().getCategories()
            Log.d("Dashboard", "Fetched ${allCategories.size} categories.")

            // Fetch category budgets and compare spending
            val db = FirebaseFirestore.getInstance()
            val categoryBudgetCollection = db.collection("users").document(userId).collection("category_budgets")
            val categoryBudgetsSnapshot = categoryBudgetCollection
                .whereEqualTo("month", Calendar.getInstance().get(Calendar.MONTH) + 1)
                .whereEqualTo("year", Calendar.getInstance().get(Calendar.YEAR))
                .get()
                .await()

            val categoryBudgets = categoryBudgetsSnapshot.documents.mapNotNull {
                it.toObject(com.aj.flourish.models.CategoryBudget::class.java)
            }

            Log.d("Dashboard", "Fetched ${categoryBudgets.size} category budgets.")

            val spendingByCategory = expensesForSelectedPeriod
                .groupBy { it.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            for (budget in categoryBudgets) {
                val spent = spendingByCategory[budget.categoryId] ?: 0.0
                if (spent > budget.budgetAmount) {
                    Log.w("Dashboard", "🚨 Category ${budget.categoryId} overspent! Spent: $spent, Budget: ${budget.budgetAmount}")
                }
            }

            // Fetch current month's budget ONLY if it's the current month we are viewing
            var currentMonthBudget: Budget? = null
            if (isCurrentMonth) {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val monthlyBudgets = BudgetRepository().getBudgetsForYear(currentYear)
                currentMonthBudget = monthlyBudgets.find { it.month == currentMonth }
                Log.d("Dashboard", "Budget for current month fetched: ${currentMonthBudget != null}")
            }

            withContext(Dispatchers.Main) {
                val totalSpending = expensesForSelectedPeriod.sumOf { it.amount }
                tvTotalSpending.text = "Total Spending (${spinnerPeriodFilter.selectedItem}): ${UserSettings.currencySymbol} ${"%.2f".format(totalSpending)}"
                Log.d("Dashboard", "Updated Total Spending text: ${tvTotalSpending.text}")

                // Update Monthly Budget display and CardView summary
                if (isCurrentMonth) {
                    currentMonthBudget?.let { budget ->
                        tvMonthlyBudget.text = "Monthly Budget: ${UserSettings.currencySymbol}${"%.2f".format(budget.minAmount)} - ${UserSettings.currencySymbol}${"%.2f".format(budget.maxAmount)}"

                        val maxBudget = budget.maxAmount
                        val remaining = maxBudget - totalSpending
                        val overspent = if (remaining < 0) -remaining else 0.0

                        tvTotalBudget.text = "${UserSettings.currencySymbol} ${String.format("%.2f", maxBudget)}"
                        tvRemainingBudget.text = "${UserSettings.currencySymbol} ${String.format("%.2f", if (remaining > 0) remaining else 0.0)}"
                        tvOverspentBudget.text = "${UserSettings.currencySymbol} ${String.format("%.2f", overspent)}"
                        Log.d("Dashboard", "Displayed current month budget details.")
                    } ?: run {
                        tvMonthlyBudget.text = "Monthly Budget: Not set"
                        tvTotalBudget.text = "${UserSettings.currencySymbol} 0.00"
                        tvRemainingBudget.text = "${UserSettings.currencySymbol} 0.00"
                        tvOverspentBudget.text = "${UserSettings.currencySymbol} 0.00"
                        Log.d("Dashboard", "No budget set for current month. Displayed defaults.")
                    }
                } else {
                    tvMonthlyBudget.text = "Budget details for current month only"
                    tvTotalBudget.text = "${UserSettings.currencySymbol} 0.00"
                    tvRemainingBudget.text = "${UserSettings.currencySymbol} 0.00"
                    tvOverspentBudget.text = "${UserSettings.currencySymbol} 0.00"
                    Log.d("Dashboard", "Cleared budget details for non-current month view.")
                }

                // Chart and transactions
                setupCategorySpendingChart(expensesForSelectedPeriod, allCategories)
                loadRecentTransactions(startDate, endDate)

                val selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val selectedYear = Calendar.getInstance().get(Calendar.YEAR)

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("category_budgets")
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val budgetDisplays = mutableListOf<CategoryBudgetDisplay>()
                        for (doc in snapshot.documents) {
                            val categoryId = doc.getString("categoryId") ?: continue
                            val budgetAmount = doc.getDouble("budgetAmount") ?: 0.0
                            val category = allCategories.find { it.id == categoryId } ?: continue
                            val spentAmount = expensesForSelectedPeriod
                                .filter { it.categoryId == categoryId }
                                .sumOf { it.amount }

                            budgetDisplays.add(CategoryBudgetDisplay(
                                categoryName = category.name,
                                budgetAmount = budgetAmount,
                                spentAmount = spentAmount
                            ))
                        }

                        Log.d("Dashboard", "Budgets to show: ${budgetDisplays.size}")

                        categoryBudgetList.clear()
                        categoryBudgetList.addAll(budgetDisplays)
                        categoryBudgetAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Dashboard", "Error loading category budgets: ${e.message}")
                    }
            }
        }
    }



    // loadRecentTransactions accepts start and end dates ---
    private fun loadRecentTransactions(startDate: Long, endDate: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Dashboard", "loadRecentTransactions executing with Start: ${startDate}, End: ${endDate}")
            // Filter for selected period's expenses and take the latest 5
            val recentExpenses = ExpenseRepository().getExpensesBetweenDates(startDate, endDate)
                .sortedByDescending { it.date }
                .take(5)
            Log.d("Dashboard", "Fetched ${recentExpenses.size} recent expenses for the selected period.")

            withContext(Dispatchers.Main) {
                recentTransactionsList.clear()
                recentTransactionsList.addAll(recentExpenses)
                recentTransactionsAdapter.notifyDataSetChanged()
                Log.d("Dashboard", "Recent transactions adapter updated.")
            }
        }
    }

    // --- Function to set up and populate the Bar Chart ---
    private fun setupCategorySpendingChart(expenses: List<Expense>, categories: List<Category>) {
        Log.d("Dashboard", "setupCategorySpendingChart called with ${expenses.size} expenses and ${categories.size} categories.")

        // 1. Aggregate expenses by category
        val categorySpending = expenses
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        Log.d("Dashboard", "Category spending map: $categorySpending")

        // 2. Prepare data for the chart
        val entries = mutableListOf<BarEntry>()
        val categoryLabels = mutableListOf<String>()

        // Sort categories by name for consistent chart appearance and label order
        val sortedCategories = categories.sortedBy { it.name }

        var i = 0f
        for (category in sortedCategories) {
            val amount = categorySpending[category.id] ?: 0.0
            if (amount > 0) { // Only add to chart if there's actual spending in the category
                entries.add(BarEntry(i, amount.toFloat()))
                categoryLabels.add(category.name)
                Log.d("Dashboard", "Adding chart entry: Category '${category.name}', Amount: $amount")
            } else {
                Log.d("Dashboard", "Category '${category.name}' has no spending in this period. Skipping chart entry.")
            }
            i++
        }

        Log.d("Dashboard", "Bar entries prepared: ${entries.size}, Labels: ${categoryLabels.size}")

        // Handle case where there's no data
        if (entries.isEmpty()) {
            categorySpendingChart.clear()
            categorySpendingChart.setNoDataText("No expenses recorded yet for this period!")
            categorySpendingChart.setNoDataTextColor(Color.BLACK) // Ensure text is visible
            Log.d("Dashboard", "No chart data. Displaying 'No expenses recorded yet for this period!'.")
            categorySpendingChart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Spending per Category").apply {
            // Use predefined colors or define your own custom colors here
            colors = ColorTemplate.MATERIAL_COLORS.toList() // Using a list directly
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        val barData = BarData(dataSet)
        categorySpendingChart.data = barData
        barData.barWidth = 0.9f

        // 3. Configure the chart appearance
        categorySpendingChart.apply {
            description.isEnabled = false // Disable description label
            setFitBars(true) // Make the bars fit perfectly in the view
            animateY(1000) // Animate bars for a smooth appearance

            // Legend configuration (explains what the bars represent)
            legend.apply {
                form = Legend.LegendForm.SQUARE
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textColor = Color.BLACK
                xEntrySpace = 8f // Spacing between legend entries
            }

            // X-axis configuration (category names)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(categoryLabels) // Assign labels to bars
                position = XAxis.XAxisPosition.BOTTOM // Place labels at the bottom
                granularity = 1f // Minimum interval between values on the axis
                setDrawGridLines(false) // Don't draw vertical grid lines
                setDrawAxisLine(true) // Draw the axis line
                labelCount = categoryLabels.size // Ensure all labels are shown
                labelRotationAngle = -45f // Rotate labels if they overlap (important for many categories)
                textColor = Color.BLACK
                textSize = 10f
            }

            // Left Y-axis configuration (spending amounts)
            axisLeft.apply {
                axisMinimum = 0f // Start from 0
                setDrawGridLines(true) // Draw horizontal grid lines
                setDrawAxisLine(true) // Draw the axis line
                textColor = Color.BLACK
                textSize = 10f
            }

            // Right Y-axis (disable, as we only need one Y-axis for amounts)
            axisRight.isEnabled = false

            invalidate() // Refresh the chart to display the new data
            Log.d("Dashboard", "Chart invalidated (refreshed).")
        }
    }
}