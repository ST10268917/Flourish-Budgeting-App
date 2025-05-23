package com.aj.flourish

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.aj.flourish.models.Category
import com.aj.flourish.repositories.CategoryRepository

class CreateCategory : AppCompatActivity() {

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
            if (name.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val category = Category(
                    name = name,
                    imageUri = selectedImageUri?.toString() ?: "",
                    userId = userId
                )

                CoroutineScope(Dispatchers.IO).launch {
                    CategoryRepository().insertCategory(category)
                    withContext(Dispatchers.Main) {
                        loadCategoriesFromDb()
                        dialog.dismiss()
                        selectedImageUri = null
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
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
}
