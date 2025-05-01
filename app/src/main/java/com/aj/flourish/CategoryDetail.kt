package com.aj.flourish

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CategoryDetail : AppCompatActivity() {
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()
    private var selectedReceiptUri: Uri? = null
    private var receiptImageView: ImageView? = null
    private var currentPhotoPath: String? = null

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    // Camera Permissions

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    private val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)
        database = AppDatabase.getInstance(this)
        expenseDao = database.expenseDao()
        val recyclerViewExpenses = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        recyclerViewExpenses.layoutManager = LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(expenseList)
        recyclerViewExpenses.adapter = expenseAdapter

        // Initialize the permission launchers
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        cameraImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoPath?.let { path ->
                    val file = File(path)
                    val internalUri = copyImageToInternalStorage(Uri.fromFile(file))
                    internalUri?.let {
                        receiptImageView?.setImageURI(it)
                        receiptImageView?.visibility = View.VISIBLE
                        selectedReceiptUri = it // Ensure selectedReceiptUri is updated here
                    } ?: run {
                        Toast.makeText(this, "Error saving captured image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        galleryImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    val internalUri = copyImageToInternalStorage(uri)
                    internalUri?.let {
                        receiptImageView?.setImageURI(it)
                        receiptImageView?.visibility = View.VISIBLE
                        selectedReceiptUri = it
                    } ?: run {
                        Toast.makeText(this, "Error saving selected image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                // Safe to proceed with camera or gallery
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

        val permissionList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        permissionLauncher.launch(permissionList)

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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.ivExpenseReceipt)
        receiptImageView = imageViewReceipt

        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)
        val btnTakePhoto = dialogView.findViewById<Button>(R.id.btnCaptureImage)
        val btnChooseGallery = dialogView.findViewById<Button>(R.id.btnSelectImage)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveExpense)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                dispatchTakePictureIntent()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    AlertDialog.Builder(this)
                        .setTitle("Camera Permission Needed")
                        .setMessage("This app needs camera permission to take receipt photos")
                        .setPositiveButton("OK") { _, _ ->
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        btnChooseGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder(this)
                        .setTitle("Storage Permission Needed")
                        .setMessage("This app needs storage permission to access your receipts")
                        .setPositiveButton("OK") { _, _ ->
                            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

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
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateText)
                date?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val categoryId = intent.getIntExtra("categoryId", -1)
            Log.d("CategoryDetail", "categoryId passed: $categoryId")

            val receiptToSave = selectedReceiptUri?.toString()

            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                description = descriptionText,
                amount = amount,
                date = selectedDateMillis,
                receiptUri = receiptToSave
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraImageLauncher.launch(takePictureIntent)
        }
    }

    private fun openGallery() {
        galleryImageLauncher.launch(pickImageIntent)
    }

    private fun getPathFromUri(uri: Uri?): String? {
        uri?.let {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return cursor.getString(columnIndex)
                }
            }
        }
        return null
    }

    fun showExpenseDetailDialog(expense: Expense) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense_detail, null)

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

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        dialog.show()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
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
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            outputStream.close()
            inputStream?.close()

            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}