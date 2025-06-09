package com.aj.flourish

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.Utils.BadgeManager // 🟡 BadgeManager for unlocking badges
import com.aj.flourish.models.Category
import com.aj.flourish.models.Expense
import com.aj.flourish.repositories.CategoryRepository
import com.aj.flourish.repositories.ExpenseRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class CategorySpendingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategorySpendingAdapter
    private val spendingList = mutableListOf<CategorySpending>()
    private lateinit var backButton: ImageView

    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var tvFilterInfo: TextView

    private var startDate: Long = 0L
    private var endDate: Long = 0L

    private val expenseRepo = ExpenseRepository()
    private val categoryRepo = CategoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_spending)

        recyclerView = findViewById(R.id.recyclerViewCategorySpending)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        tvFilterInfo = findViewById(R.id.tvFilterInfo)
        backButton = findViewById(R.id.ivBack)

        backButton.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategorySpendingAdapter(spendingList)
        recyclerView.adapter = adapter

        etStartDate.setOnClickListener { showDatePicker(true) }
        etEndDate.setOnClickListener { showDatePicker(false) }

        findViewById<Button>(R.id.btnFilterCategorySpending).setOnClickListener {
            if (etStartDate.text.isNullOrEmpty() || etEndDate.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val expenses = expenseRepo.getExpensesBetweenDates(startDate, endDate)
                val categories = categoryRepo.getCategories()
                val categoryMap = categories.associateBy({ it.id }, { it.name })

                val grouped = expenses.groupBy { it.categoryId }
                    .map { (categoryId, group) ->
                        val name = categoryMap[categoryId] ?: "Unknown"
                        CategorySpending(name, group.sumOf { it.amount })
                    }

                withContext(Dispatchers.Main) {
                    spendingList.clear()
                    spendingList.addAll(grouped)
                    adapter.notifyDataSetChanged()
                    tvFilterInfo.text = "Showing spending from ${etStartDate.text} to ${etEndDate.text}"

                    // 🟢 Unlock the "Filter Explorer" badge after using the filter
                    BadgeManager.checkAndUnlockBadge(this@CategorySpendingActivity, "filter_test")
                }
            }
        }

        // 🔄 Load default (all-time) data when activity starts
        CoroutineScope(Dispatchers.IO).launch {
            val expenses = expenseRepo.getExpensesForUser()
            val categories = categoryRepo.getCategories()
            val categoryMap = categories.associateBy({ it.id }, { it.name })

            val grouped = expenses.groupBy { it.categoryId }
                .map { (categoryId, group) ->
                    val name = categoryMap[categoryId] ?: "Unknown"
                    CategorySpending(name, group.sumOf { it.amount })
                }

            withContext(Dispatchers.Main) {
                spendingList.clear()
                spendingList.addAll(grouped)
                adapter.notifyDataSetChanged()
                tvFilterInfo.text = "Showing all-time spending"
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            if (isStartDate) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                this.startDate = calendar.timeInMillis
                etStartDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate)))
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                this.endDate = calendar.timeInMillis
                etEndDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate)))
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
