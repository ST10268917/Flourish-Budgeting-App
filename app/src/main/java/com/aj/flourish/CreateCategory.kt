package com.aj.flourish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class CreateCategory : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private val categoryList = mutableListOf<Category>()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var backButton: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var imageViewInDialog: ImageView? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddCategory: FloatingActionButton

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        database = AppDatabase.getInstance(this)
        categoryDao = database.categoryDao()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        // Link views
        recyclerView = findViewById(R.id.recyclerViewCategories)
        fabAddCategory = findViewById(R.id.btnAddCategory)
        backButton = findViewById(R.id.ivBack)

        // Setup RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 4) // 4 items per row
        recyclerView.setHasFixedSize(true)

        // Setup adapter
        categoryAdapter = CategoryAdapter(categoryList)
        recyclerView.adapter = categoryAdapter


        // Add Category Button Click
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
        loadCategoriesFromDb()
    }

    private fun loadCategoriesFromDb() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val categoriesFromDb = categoryDao.getCategoriesForUser(userId)

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
        val imageView = dialogView.findViewById<ImageView>(R.id.ivCategoryImage)
        imageViewInDialog = imageView
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveCategory)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        imageView.setOnClickListener {
            // Pick image from gallery
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSave.setOnClickListener {
            val name = editTextName.text.toString().trim()

            if (name.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val category = Category(
                    name = name,
                    imageUri = selectedImageUri?.toString() ?: "", // Store empty if no image
                    userId = userId
                )
                CoroutineScope(Dispatchers.IO).launch {
                    categoryDao.insertCategory(category)

                    withContext(Dispatchers.Main) {
                        loadCategoriesFromDb()
                        dialog.dismiss()
                        selectedImageUri = null // Reset after saving
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val originalUri = data?.data

            if (originalUri != null) {
                val copiedUri = copyImageToInternalStorage(originalUri)

                if (copiedUri != null) {
                    selectedImageUri = copiedUri
                    imageViewInDialog?.setImageURI(selectedImageUri)
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
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

