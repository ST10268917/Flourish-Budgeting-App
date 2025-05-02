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
    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>

    private val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    private val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)

        database = AppDatabase.getInstance(this)
        expenseDao = database.expenseDao()

        val recyclerViewExpenses = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        recyclerViewExpenses.layoutManager = LinearLayoutManager(this)

        expenseAdapter = ExpenseAdapter(expenseList) { expense ->
            showExpenseDetailDialog(expense)
        }
        recyclerViewExpenses.adapter = expenseAdapter

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
                    } else {
                        Toast.makeText(this, "Captured image file not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        galleryImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri == null) {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    Log.e("GALLERY_PICKED", "No image URI returned.")
                    return@registerForActivityResult
                }

                Log.d("GALLERY_PICKED", "Selected URI: $selectedImageUri")

                try {
                    contentResolver.takePersistableUriPermission(
                        selectedImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.w("GALLERY_PICKED", "Persistable URI permission not granted: ${e.message}")
                }

                val internalUri = copyImageToInternalStorage(selectedImageUri)
                if (internalUri != null) {
                    receiptImageView?.setImageURI(internalUri)
                    receiptImageView?.visibility = View.VISIBLE
                    selectedReceiptUri = internalUri
                } else {
                    Log.e("GALLERY_ERROR", "copyImageToInternalStorage failed for URI: $selectedImageUri")
                    Toast.makeText(this, "Error saving selected image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("GALLERY_PICKED", "Image selection was canceled or failed.")
                Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val dateString = String.format("%04d-%02d-%02d", year, month + 1, day)
                etDate.setText(dateString)
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
                val date = sdf.parse(dateText)
                date?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val categoryId = intent.getIntExtra("categoryId", -1)

            val receiptToSave = selectedReceiptUri?.toString()

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

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }

        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            cameraImageLauncher.launch(intent)
        }
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        galleryImageLauncher.launch(intent)
    }


    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            Log.d("GALLERY_URI", "Trying to copy URI: $uri")

            val inputStream = when (uri.scheme) {
                "content" -> contentResolver.openInputStream(uri)
                "file" -> {
                    val file = File(uri.path!!)
                    Log.d("GALLERY_DEBUG", "File exists: ${file.exists()}, Path: ${file.absolutePath}")
                    file.inputStream()
                }
                else -> null
            } ?: throw IOException("Unable to open input stream for URI: $uri")

            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            outputStream.close()
            inputStream.close()

            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e("GALLERY_COPY_ERROR", "Failed to copy image from URI: $uri")
            Log.e("GALLERY_COPY_ERROR", "Exception: ${e.message}")
            e.printStackTrace()
            null
        }
    }







    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun showExpenseDetailDialog(expense: Expense) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense_detail, null)

        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewDetailDate)
        val textViewAmount = dialogView.findViewById<TextView>(R.id.textViewDetailAmount)
        val textViewDescription = dialogView.findViewById<TextView>(R.id.textViewDetailDescription)
        val imageViewReceipt = dialogView.findViewById<ImageView>(R.id.imageViewDetailReceipt)

        textViewDate.text = "Date: ${formatDate(expense.date)}"
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
