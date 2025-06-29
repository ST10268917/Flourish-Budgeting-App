package com.aj.flourish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import com.aj.flourish.supabase.prepareImageForUpload
import okhttp3.RequestBody.Companion.toRequestBody
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import com.aj.flourish.base.BaseActivity
import com.aj.flourish.models.Category
import com.aj.flourish.models.CategoryBudget
import com.aj.flourish.repositories.CategoryRepository
import com.aj.flourish.supabase.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.Calendar

class CreateCategory : BaseActivity() {

    private val categoryList = mutableListOf<Category>()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddCategory: FloatingActionButton

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var imageViewInDialog: ImageView? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        recyclerView = findViewById(R.id.recyclerViewCategories)
        fabAddCategory = findViewById(R.id.btnAddCategory)
        backButton = findViewById(R.id.ivBack)

        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.setHasFixedSize(true)

        categoryAdapter = CategoryAdapter(categoryList)
        recyclerView.adapter = categoryAdapter

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val originalUri = result.data?.data
                if (originalUri != null) {
                    val copiedUri = copyImageToInternalStorage(originalUri)
                    if (copiedUri != null) {
                        selectedImageUri = copiedUri
                        imageViewInDialog?.setImageURI(copiedUri)
                    } else {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        loadCategoriesFromDb()
    }

    private fun loadCategoriesFromDb() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val categoriesFromDb = CategoryRepository().getCategories()
            withContext(Dispatchers.Main) {
                categoryList.clear()
                categoryList.addAll(categoriesFromDb)
                categoryAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val etBudgetAmount = dialogView.findViewById<EditText>(R.id.etCategoryBudget)
        val imageView = dialogView.findViewById<ImageView>(R.id.ivCategoryImage)
        imageViewInDialog = imageView
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveCategory)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val budgetText = etBudgetAmount.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (budgetText.isEmpty()) {
                Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val budgetAmount = budgetText.toDoubleOrNull() ?: 0.0

            // --- All your existing logic below here ---
            CoroutineScope(Dispatchers.IO).launch {
                var imageUrl = ""

                selectedImageUri?.let { uri ->
                    val bytes = getBytesFromUri(uri)
                    if (bytes != null) {
                        val fileName = "category_${System.currentTimeMillis()}.jpg"
                        imageUrl = uploadToSupabase(bytes, fileName) ?: ""
                    }
                }

                val db = FirebaseFirestore.getInstance()

                // Create category under the current user
                val categoryRef = db.collection("users")
                    .document(userId)
                    .collection("categories")
                    .document()
                val categoryId = categoryRef.id

                val category = Category(
                    id = categoryId,
                    name = name,
                    imageUri = imageUrl,
                    userId = userId
                )

                // Save category under user's categories
                categoryRef.set(category)
                    .addOnSuccessListener {
                        Log.d("CreateCategory", "Category saved successfully!")

                        val calendar = Calendar.getInstance()
                        val month = calendar.get(Calendar.MONTH) + 1
                        val year = calendar.get(Calendar.YEAR)

                        val categoryBudget = CategoryBudget(
                            userId = userId,
                            categoryId = categoryId,
                            month = month,
                            year = year,
                            budgetAmount = budgetAmount
                        )

                        // Save category budget under user's category_budgets
                        db.collection("users")
                            .document(userId)
                            .collection("category_budgets")
                            .add(categoryBudget)
                            .addOnSuccessListener {
                                Log.d("CreateCategory", "Category budget saved successfully!")
                            }
                            .addOnFailureListener { e ->
                                Log.e("CreateCategory", "Failed to save category budget: ${e.message}")
                            }

                        CoroutineScope(Dispatchers.Main).launch {
                            loadCategoriesFromDb()
                            dialog.dismiss()
                            selectedImageUri = null
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CreateCategory", "Failed to save category: ${e.message}")
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(this@CreateCategory, "Failed to save category.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        dialog.show()
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "category_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            outputStream.close()
            inputStream.close()

            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getBytesFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadToSupabase(
        byteArray: ByteArray,
        fileName: String,
        bucket: String = "category-images"
    ): String? {
        return withContext(Dispatchers.IO) {
            val mimeType = "image/jpeg"
            val authHeader = "Bearer ${BuildConfig.SUPABASE_KEY}"

            val body = byteArray.toRequestBody(mimeType.toMediaTypeOrNull())

            val response = RetrofitClient.instance.uploadImage(
                authHeader = authHeader,
                contentType = mimeType,
                bucket = bucket,
                fileName = fileName,
                file = body
            )

            Log.d("SUPABASE_DEBUG", "SUPABASE_URL: ${BuildConfig.SUPABASE_URL}")
            Log.d("SUPABASE_DEBUG", "SUPABASE_KEY: ${BuildConfig.SUPABASE_KEY}")

            Log.d("SUPABASE_UPLOAD", "Response: ${response.code()} | ${response.message()}")

            if (response.isSuccessful) {
                "https://cbwdbipbwbyxszjmibnm.supabase.co/storage/v1/object/public/$bucket/$fileName"
            } else {
                null
            }
        }
    }
}
