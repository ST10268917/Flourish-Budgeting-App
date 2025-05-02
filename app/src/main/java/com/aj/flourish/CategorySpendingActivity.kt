package com.aj.flourish

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class CategorySpendingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategorySpendingAdapter
    private val spendingList = mutableListOf<CategorySpending>()

    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var tvFilterInfo: TextView

    private var startDate: Long = 0L
    private var endDate: Long = 0L

    private lateinit var expenseDao: ExpenseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_spending)

        expenseDao = AppDatabase.getInstance(this).expenseDao()

        recyclerView = findViewById(R.id.recyclerViewCategorySpending)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        tvFilterInfo = findViewById(R.id.tvFilterInfo)

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

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            CoroutineScope(Dispatchers.IO).launch {
                val result = expenseDao.getSpendingPerCategory(userId, startDate, endDate)
                withContext(Dispatchers.Main) {
                    spendingList.clear()
                    spendingList.addAll(result)
                    adapter.notifyDataSetChanged()

                    tvFilterInfo.text = "Showing spending from ${etStartDate.text} to ${etEndDate.text}"
                }
            }
        }

        // Default: show all-time spending
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val result = expenseDao.getSpendingPerCategory(it, 0L, Long.MAX_VALUE)
                withContext(Dispatchers.Main) {
                    spendingList.clear()
                    spendingList.addAll(result)
                    adapter.notifyDataSetChanged()
                    tvFilterInfo.text = "Showing all-time spending"
                }
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
                startDate = calendar.timeInMillis
                etStartDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate)))
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endDate = calendar.timeInMillis
                etEndDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate)))
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
