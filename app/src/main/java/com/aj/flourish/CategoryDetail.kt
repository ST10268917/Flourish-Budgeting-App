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
import android.media.MediaScannerConnection
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aj.flourish.models.Expense
import com.aj.flourish.repositories.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CategoryDetail : AppCompatActivity() {
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()
    private var selectedReceiptUri: Uri? = null
    private var receiptImageView: ImageView? = null
    private var currentPhotoPath: String? = null
    private lateinit var backButton: ImageView

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

        backButton = findViewById(R.id.ivBack)
        val recyclerViewExpenses = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        recyclerViewExpenses.layoutManager = LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(expenseList) { expense ->
            showExpenseDetailDialog(expense)
        }
        recyclerViewExpenses.adapter = expenseAdapter

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) dispatchTakePictureIntent()
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

        storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openGallery()
            else Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, CreateCategory::class.java))
        }

        cameraImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null) { _, _ ->
                            runOnUiThread {
                                val internalUri = copyImageToInternalStorage(Uri.fromFile(file))
                                internalUri?.let {
                                    selectedReceiptUri = it
                                    receiptImageView?.setImageURI(it)
                                    receiptImageView?.visibility = View.VISIBLE
                                } ?: run {
                                    Toast.makeText(this, "Error saving captured image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        galleryImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri != null) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            selectedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {}

                    val internalUri = copyImageToInternalStorage(selectedImageUri)
                    if (internalUri != null) {
                        receiptImageView?.setImageURI(internalUri)
                        receiptImageView?.visibility = View.VISIBLE
                        selectedReceiptUri = internalUri
                    } else {
                        Toast.makeText(this, "Error saving selected image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val btnAddExpense = findViewById<Button>(R.id.btnAddExpense)
        btnAddExpense.setOnClickListener { showAddExpenseDialog() }

        val imageView = findViewById<ImageView>(R.id.imageViewCategoryDetail)
        val textView = findViewById<TextView>(R.id.textViewCategoryNameDetail)
        val categoryName = intent.getStringExtra("categoryName")
        val categoryImageUri = intent.getStringExtra("categoryImageUri")

        textView.text = categoryName
        if (categoryImageUri != null) imageView.setImageURI(Uri.parse(categoryImageUri))

        loadExpensesFromFirebase()
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

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnChooseGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
                storagePermissionLauncher.launch(permission)
            }
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSave.setOnClickListener {
            val dateText = etDate.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val descriptionText = etDescription.text.toString().trim()

            if (dateText.isEmpty() || amountText.isEmpty() || descriptionText.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDateMillis = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.parse(dateText)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val categoryId = intent.getStringExtra("categoryId") ?: return@setOnClickListener

            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                description = descriptionText,
                amount = amount,
                date = selectedDateMillis,
                receiptUri = selectedReceiptUri?.toString()
            )

            CoroutineScope(Dispatchers.IO).launch {
                ExpenseRepository().insertExpense(expense)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    loadExpensesFromFirebase()
                    Toast.makeText(this@CategoryDetail, "Expense saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun loadExpensesFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val categoryId = intent.getStringExtra("categoryId") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val expenses = ExpenseRepository().getExpensesForCategory(categoryId)
            withContext(Dispatchers.Main) {
                expenseList.clear()
                expenseList.addAll(expenses)
                expenseAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace(); null
        }

        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", it)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }

            val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            cameraImageLauncher.launch(intent)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        galleryImageLauncher.launch(intent)
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        } catch (e: Exception) {
            Log.e("GALLERY_COPY_ERROR", "Failed to copy image: ${e.message}")
            null
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun showExpenseDetailDialog(expense: Expense) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense_detail, null)

        dialogView.findViewById<TextView>(R.id.textViewDetailDate).text = "Date: ${formatDate(expense.date)}"
        dialogView.findViewById<TextView>(R.id.textViewDetailAmount).text = "Amount: ${UserSettings.currencySymbol}${"%.2f".format(expense.amount)}"
        dialogView.findViewById<TextView>(R.id.textViewDetailDescription).text = "Description: ${expense.description}"

        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.imageViewDetailReceipt)
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }


}
