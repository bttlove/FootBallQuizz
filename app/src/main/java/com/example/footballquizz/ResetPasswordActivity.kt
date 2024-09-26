package com.example.footballquizz
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var inputEmail: EditText
    private lateinit var btnReset: Button
    private lateinit var btnBack: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        inputEmail = findViewById(R.id.email)
        btnReset = findViewById(R.id.btn_reset_password)
        btnBack = findViewById(R.id.btn_back)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        btnBack.setOnClickListener {
            finish()
        }

        btnReset.setOnClickListener {
            val email = inputEmail.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter your registered email id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send reset email!", Toast.LENGTH_SHORT).show()
                    }

                    progressBar.visibility = View.GONE
                }
        }
    }
}