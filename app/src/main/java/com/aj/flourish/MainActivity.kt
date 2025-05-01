package com.aj.flourish

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // Declare two lateinit variables for the login and sign-up buttons
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()// Enables edge-to-edge layout
        setContentView(R.layout.activity_main)// Sets the layout file for this activity

        // Initialize the buttons by finding them from the layout
        loginBtn = findViewById(R.id.btnLogin)
        signUpBtn = findViewById(R.id.btnSignUp)


        // Set a click listener on the login button to navigate to LoginActivity
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
// Set a click listener on the sign-up button to navigate to SignUp activity
        signUpBtn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }


}

/** References - Code attribution
- Android Developers (n.d.) *CameraX configuration guide*. Available at: https://developer.android.com/media/camera/camerax/configuration (Accessed: 1 June 2024).
- GitHub Marketplace (n.d.) *Automated Android Build Action*. Available at: https://github.com/marketplace/actions/automated-build-android-app-with-github-action (Accessed: 1 June 2024).
- Google Fonts (n.d.) *Material Icons*. Available at: https://fonts.google.com/icons (Accessed: 1 June 2024).
- IMAD5112 (2023) *GitHub Actions workflow for Android*. Available at: https://github.com/IMAD5112/Github-actions/blob/main/.github/workflows/build.yml (Accessed: 1 June 2024).
*/