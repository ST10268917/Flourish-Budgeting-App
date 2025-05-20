package com.aj.flourish

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var mobile: EditText
    private lateinit var currencySpinner: Spinner
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginText: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        username = findViewById(R.id.username)
        email = findViewById(R.id.etEmail)
        mobile = findViewById(R.id.etMobile)
        currencySpinner = findViewById(R.id.spinnerCurrency)
        password = findViewById(R.id.etPassword)
        confirmPassword = findViewById(R.id.etConfirmPassword)
        signUpButton = findViewById(R.id.btnSignUp)
        loginText = findViewById(R.id.tvLogin)

        signUpButton.setOnClickListener {
            signUpUser()
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val currencies = resources.getStringArray(R.array.currencies)
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter
        currencySpinner.setSelection(0)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
    }

    private fun signUpUser() {
        val usernameText = username.text.toString().trim()
        val emailText = email.text.toString().trim()
        val mobileText = mobile.text.toString().trim()
        val selectedCurrency = currencySpinner.selectedItem.toString()
        val passwordText = password.text.toString().trim()
        val confirmPasswordText = confirmPassword.text.toString().trim()

        if (emailText.isEmpty() || passwordText.isEmpty() || usernameText.isEmpty() || mobileText.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordText != confirmPasswordText) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SignUp", "Attempting to create user with email: $emailText")

        auth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    Log.d("SignUp", "User created successfully: $userId")

                    val userMap = hashMapOf(
                        "username" to usernameText,
                        "email" to emailText,
                        "mobile" to mobileText,
                        "currency" to selectedCurrency
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId!!)
                        .set(userMap)
                        .addOnSuccessListener {
                            Log.d("Firestore", "User data saved in Firestore under ID: $userId")
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Dashboard::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error saving user data", e)
                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                } else {
                    Log.e("SignUp", "User creation failed", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
