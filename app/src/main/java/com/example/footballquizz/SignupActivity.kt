package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SignupActivity : AppCompatActivity() {
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: Button
    private lateinit var btnResetPassword: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Find views by ID
        btnSignIn = findViewById(R.id.sign_in_button)
        btnSignUp = findViewById(R.id.sign_up_button)
        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBar)
        btnResetPassword = findViewById(R.id.btn_reset_password)

        // Set onClick listeners
        btnResetPassword.setOnClickListener {
            startActivity(Intent(this@SignupActivity, ResetPasswordActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        }

        btnSignUp.setOnClickListener {
            createAccount()
        }
    }
    private fun createAccount() {
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            showToast("Enter email address!")
            return
        }

        if (TextUtils.isEmpty(password)) {
            showToast("Enter password!")
            return
        }

        if (password.length < 6) {
            showToast("Password too short, enter minimum 6 characters!")
            return
        }

        progressBar.visibility = View.VISIBLE

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Get current date-time
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val formattedDateTime = currentDateTime.format(formatter)

                    val role = if (email.endsWith("@admin.com")) "admin" else "user"
                    // Create user data to store in Firestore
                    val userData = hashMapOf(
                        "e-mail" to email,
                        "role" to "user", // Default role
                        "date-time" to currentDateTime.toString(),
                        "name" to "", // You can prompt the user to enter their name later
                        "picture" to "" // Placeholder for profile picture
                    )

                    // Get Firestore instance
                    val db = FirebaseFirestore.getInstance()

                    // Add user data to Firestore under the "auths" collection
                    if (userId != null) {
                        db.collection("auths").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                showToast("Account created and saved successfully.")
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showToast("Failed to save user data: ${e.message}")
                            }
                    } else {
                        showToast("Error: Could not get user ID.")
                    }

                } else {
                    showToast("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
    }
}
