package com.aj.flourish

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.Utils.BadgeManager
import com.aj.flourish.base.BaseActivity
import com.aj.flourish.models.Expense
import com.aj.flourish.repositories.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FilterExpensesActivity : BaseActivity() {
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private val expenseList = mutableListOf<Expense>()
    private lateinit var startDateField: EditText
    private lateinit var endDateField: EditText

    private var startDate: Long = 0L
    private var endDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_expenses)

        recyclerView = findViewById(R.id.recyclerViewFilteredExpenses)
        startDateField = findViewById(R.id.etStartDate)
        endDateField = findViewById(R.id.etEndDate)

        recyclerView.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(expenseList) { expense ->
            showExpenseDetailDialog(expense)
        }
        recyclerView.adapter = expenseAdapter
        backButton = findViewById(R.id.ivBack)
        backButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        startDateField.setOnClickListener { showDatePicker(true) }
        endDateField.setOnClickListener { showDatePicker(false) }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Load all expenses
        CoroutineScope(Dispatchers.IO).launch {
            val allExpenses = ExpenseRepository().getExpensesForUser()
            withContext(Dispatchers.Main) {
                expenseList.clear()
                expenseList.addAll(allExpenses)
                expenseAdapter.notifyDataSetChanged()
            }
        }

        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            if (startDate == 0L || endDate == 0L) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val filteredExpenses = ExpenseRepository().getExpensesBetweenDates(startDate, endDate)

                // 🔥 Unlock test badge after filtering
                withContext(Dispatchers.Main) {
                    expenseList.clear()
                    expenseList.addAll(filteredExpenses)
                    expenseAdapter.notifyDataSetChanged()

                    Toast.makeText(this@FilterExpensesActivity, "Filtered ${filteredExpenses.size} expenses", Toast.LENGTH_SHORT).show()


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
                startDateField.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate)))
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                endDate = calendar.timeInMillis
                endDateField.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate)))
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showExpenseDetailDialog(expense: Expense) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_expense_detail, null)

        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewDetailDate)
        val textViewAmount = dialogView.findViewById<TextView>(R.id.textViewDetailAmount)
        val textViewDescription = dialogView.findViewById<TextView>(R.id.textViewDetailDescription)
        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.imageViewDetailReceipt)

        textViewDate.text = "Date: ${SimpleDateFormat("dd MMM yyyy").format(Date(expense.date))}"
        textViewAmount.text = "Amount: ${UserSettings.currencySymbol}${"%.2f".format(expense.amount)}"
        textViewDescription.text = "Description: ${expense.description}"

        if (!expense.receiptUri.isNullOrEmpty()) {
            imageViewReceipt.setImageURI(Uri.parse(expense.receiptUri))
        } else {
            imageViewReceipt.setImageResource(android.R.color.darker_gray)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()
            .show()
    }
}
