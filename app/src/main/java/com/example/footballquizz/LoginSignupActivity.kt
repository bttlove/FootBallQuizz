package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginSignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loging_singup)

        // Find the buttons in the layout
        val loginButton = findViewById<Button>(R.id.btn_login)
        val signUpButton = findViewById<Button>(R.id.btn_signup)

        // Handle Login button click
        loginButton.setOnClickListener {
            val intent = Intent(this@LoginSignupActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Handle Sign Up button click
        signUpButton.setOnClickListener {
            val intent = Intent(this@LoginSignupActivity, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
