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


    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        loginBtn = findViewById(R.id.btnLogin)
        signUpBtn = findViewById(R.id.btnSignUp)



        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}

/*Code Attribution for the app
CameraX implementation inspired by:
Android Developers. n.d.-a. CameraX overview | Android media | Android Developers. [online] Available at: https://developer.android.com/training/camerax [Accessed 2 May 2025].
UI elements or layout ideas potentially influenced by:
Browse Fonts - Google Fonts. n.d. Browse Fonts - Google Fonts. [online] Available at: https://fonts.google.com/ [Accessed 2 May 2025].  * GitHub Actions workflow inspired by:
automated-build-android-app-with-github-action:
IMAD5112. n.d. Github-actions/.github/workflows/build.yml. [online] Available at: https://github.com/IMAD5112/Github-actions/blob/main/.github/workflows/build.yml [Accessed 2 May 2025]. */