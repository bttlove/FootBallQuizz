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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
class SignupActivity {


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

            // Lấy instance FirebaseAuth
            auth = FirebaseAuth.getInstance()

            btnSignIn = findViewById(R.id.sign_in_button)
            btnSignUp = findViewById(R.id.sign_up_button)
            inputEmail = findViewById(R.id.email)
            inputPassword = findViewById(R.id.password)
            progressBar = findViewById(R.id.progressBar)
            btnResetPassword = findViewById(R.id.btn_reset_password)

            btnResetPassword.setOnClickListener {
                startActivity(Intent(this@SignupActivity, ResetPasswordActivity::class.java))
            }

            btnSignIn.setOnClickListener {
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            }

            btnSignUp.setOnClickListener {
                val email = inputEmail.text.toString().trim()
                val password = inputPassword.text.toString().trim()

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.length < 6) {
                    Toast.makeText(applicationContext, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                progressBar.visibility = View.VISIBLE

                // Tạo user mới
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignupActivity) { task ->
                        Toast.makeText(this@SignupActivity, "createUserWithEmail:onComplete:" + task.isSuccessful, Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE

                        if (!task.isSuccessful) {
                            Toast.makeText(this@SignupActivity, "Authentication failed." + task.exception, Toast.LENGTH_SHORT).show()
                        } else {
                            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                            finish()
                        }
                    }
            }
        }

        override fun onResume() {
            super.onResume()
            progressBar.visibility = View.GONE
        }
    }

}