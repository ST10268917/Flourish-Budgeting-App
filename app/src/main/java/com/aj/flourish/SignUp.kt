package com.aj.flourish

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

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

        // Sign Up button clicked
        signUpButton.setOnClickListener {
            signUpUser()
        }

        // Navigate to Login if already have an account
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val spinner: Spinner = findViewById(R.id.spinnerCurrency)
        val currencies = resources.getStringArray(R.array.currencies)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            currencies
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (hint)
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
        spinner.adapter = adapter

        spinner.setSelection(0)
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

        auth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registered successfully
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                } else {
                    // If sign up fails
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

}