package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar

import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnCompleteListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LoginActivity :  AppCompatActivity() {
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSignup: Button
    private lateinit var btnLogin: Button
    private lateinit var btnReset: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get Firebase auth instance
        auth = FirebaseAuth.getInstance()
        auth.signOut()

        db = FirebaseFirestore.getInstance()
        // Nếu người dùng đã đăng nhập, chuyển tiếp sang
        if (auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, DifficultySelectionActivity::class.java))
            finish()
        }

        // Set layout view cho LoginActivity
        setContentView(R.layout.activity_login)



        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBar)
        btnSignup = findViewById(R.id.btn_signup)
        btnLogin = findViewById(R.id.btn_login)
        btnReset = findViewById(R.id.btn_reset_password)

        // Firebase auth instance
        auth = FirebaseAuth.getInstance()

        btnSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

        btnReset.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            // Kiểm tra nhập liệu
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            // Xác thực người dùng
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    progressBar.visibility = View.GONE
                    if (!task.isSuccessful) {
                        // Nếu đăng nhập không thành công
                        if (password.length < 6) {
                            inputPassword.error = getString(R.string.minimum_password)
                        } else {
                            Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                        }
                    } else {


                        // Lấy thời gian hiện tại
                        val currentDateTime = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val formattedDateTime = currentDateTime.format(formatter)

                        // Tạo map dữ liệu đăng nhập
                        val user = hashMapOf(
                            "e-mail" to email,
                            "role" to "user", // Hoặc thay đổi role theo nhu cầu của bạn
                            "start time" to currentDateTime.toString(),
                            "end time" to currentDateTime.toString()
                        )



                        // Thêm document vào collection "login" trên Firestore
                        db.collection("login")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(this@LoginActivity, "Login data saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@LoginActivity, "Error saving login data: $e", Toast.LENGTH_SHORT).show()
                            }

                        // Chuyển tới màn hình DifficultySelectionActivity sau khi đăng nhập thành công
                        val intent = Intent(this@LoginActivity, DifficultySelectionActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng LoginActivity
                    }
                }
        }

    }
}