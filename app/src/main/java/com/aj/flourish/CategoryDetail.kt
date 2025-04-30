package com.aj.flourish

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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class CategoryDetail : AppCompatActivity() {
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()
    private val PICK_RECEIPT_IMAGE_REQUEST = 2
    private var selectedReceiptUri: Uri? = null
    private var receiptImageView: ImageView? = null

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)
        database = AppDatabase.getInstance(this)
        expenseDao = database.expenseDao()
        val recyclerViewExpenses = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        recyclerViewExpenses.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(expenseList) // Initialize it here
        recyclerViewExpenses.adapter = expenseAdapter


        val btnAddExpense = findViewById<Button>(R.id.btnAddExpense)
        btnAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        val imageView = findViewById<ImageView>(R.id.imageViewCategoryDetail)
        val textView = findViewById<TextView>(R.id.textViewCategoryNameDetail)

        // Get data from Intent
        val categoryName = intent.getStringExtra("categoryName")
        val categoryImageUri = intent.getStringExtra("categoryImageUri")

        textView.text = categoryName

        if (categoryImageUri != null) {
            imageView.setImageURI(Uri.parse(categoryImageUri))
        }
        loadExpensesFromDb()
    }
    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.ivExpenseReceipt)
        receiptImageView = imageViewReceipt // Save it globally

        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)
        imageViewReceipt.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_RECEIPT_IMAGE_REQUEST)
        }
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveExpense)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Date Picker for date field
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dateString = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    etDate.setText(dateString)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            val dateText = etDate.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val descriptionText = etDescription.text.toString().trim()

            if (dateText.isEmpty()) {
                etDate.error = "Date is required"
                return@setOnClickListener
            }

            if (amountText.isEmpty()) {
                etAmount.error = "Amount is required"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter a valid amount"
                return@setOnClickListener
            }

            if (descriptionText.isEmpty()) {
                etDescription.error = "Description is required"
                return@setOnClickListener
            }

            val selectedDateMillis = try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val date = sdf.parse(dateText)
                date?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val categoryId = intent.getIntExtra("categoryId", -1)
            Log.d("CategoryDetail", "categoryId passed: $categoryId")

            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                description = descriptionText,
                amount = amount,
                date = selectedDateMillis,
                receiptUri = selectedReceiptUri?.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                expenseDao.insertExpense(expense)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    loadExpensesFromDb()
                    Toast.makeText(this@CategoryDetail, "Expense saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_RECEIPT_IMAGE_REQUEST) {
                val originalUri = data?.data
                if (originalUri != null) {
                    val copiedUri = copyImageToInternalStorage(originalUri)
                    if (copiedUri != null) {
                        selectedReceiptUri = copiedUri
                        receiptImageView?.setImageURI(selectedReceiptUri)
                    } else {
                        Toast.makeText(this, "Failed to copy image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun showExpenseDetailDialog(expense: Expense) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_expense_detail, null)

        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewDetailDate)
        val textViewAmount = dialogView.findViewById<TextView>(R.id.textViewDetailAmount)
        val textViewDescription = dialogView.findViewById<TextView>(R.id.textViewDetailDescription)
        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.imageViewDetailReceipt)

        textViewDate.text = "Date: ${formatDate(expense.date)}"
        textViewAmount.text = "${UserSettings.currencySymbol} ${expense.amount}0"
        textViewDescription.text = "Description: ${expense.description}"

        Log.d("ExpenseDetail", "Receipt URI: ${expense.receiptUri}")

        if (!expense.receiptUri.isNullOrEmpty()) {
            imageViewReceipt.setImageURI(Uri.parse(expense.receiptUri))
        } else {
            imageViewReceipt.setImageResource(android.R.color.darker_gray) // Default if no receipt
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        dialog.show()

    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun loadExpensesFromDb() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val categoryId = intent.getIntExtra("categoryId", -1)

        CoroutineScope(Dispatchers.IO).launch {
            val expensesFromDb = expenseDao.getExpensesForUserAndCategory(userId, categoryId)

            withContext(Dispatchers.Main) {
                expenseList.clear()
                expenseList.addAll(expensesFromDb)
                expenseAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            outputStream.close()
            inputStream?.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}