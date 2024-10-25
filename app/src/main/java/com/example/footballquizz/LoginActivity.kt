package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
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

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        db = FirebaseFirestore.getInstance()

        // Set layout for LoginActivity
        setContentView(R.layout.activity_login)

        // Initialize views
        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBar)
        btnSignup = findViewById(R.id.btn_signup)
        btnLogin = findViewById(R.id.btn_login)
        btnReset = findViewById(R.id.btn_reset_password)

        // Kiểm tra nếu người dùng đã đăng nhập
        if (auth.currentUser != null) {
            val currentEmail = auth.currentUser?.email
            if (currentEmail != null) {
                checkUserRole(currentEmail)
            } else {
                auth.signOut()
                Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to SignupActivity
        btnSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

        // Navigate to ResetPasswordActivity
        btnReset.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
        }

        // Login button click listener
        // Login button click listener
        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Xác thực mật khẩu trước
            progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Nếu xác thực thành công, mới kiểm tra vai trò người dùng
                        checkUserRole(email)
                    } else {
                        // Hiển thị lỗi nếu xác thực thất bại
                        Toast.makeText(this@LoginActivity, "Authentication failed!", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    // Hàm kiểm tra trạng thái chặn của người chơi
    private fun checkBlockStatus(email: String) {
        db.collection("login").document(email).get()
            .addOnSuccessListener { document ->
                val blockUntil = document.getString("block_until")
                if (blockUntil != null) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val blockDate = sdf.parse(blockUntil)
                    val currentDate = Date()

                    // So sánh thời gian hiện tại và thời gian hết chặn
                    if (currentDate.before(blockDate)) {
                        val daysLeft = ((blockDate.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
                        Toast.makeText(this, "Bạn bị chặn trong $daysLeft ngày.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    } else {
                        // Nếu hết thời gian chặn, tiếp tục kiểm tra vai trò người dùng
                        checkUserRole(email)
                    }
                } else {
                    // Nếu không có chặn, tiếp tục kiểm tra vai trò người dùng
                    checkUserRole(email)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi kiểm tra trạng thái chặn: $e", Toast.LENGTH_SHORT).show()
            }
    }



    // Kiểm tra vai trò của người dùng
    private fun checkUserRole(email: String) {
        db.collection("user").document(email).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    val blockUntil = document.getString("block_until")

                    if (blockUntil != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val blockDate = sdf.parse(blockUntil)
                        val currentDate = Date()

                        // Nếu người dùng đang bị chặn
                        if (currentDate.before(blockDate)) {
                            val daysLeft = ((blockDate.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
                            Toast.makeText(this, "Tài khoản của bạn đã bị chặn trong $daysLeft ngày.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                    }

                    if (role == "admin") {
                        // Chuyển đến HomeAdminActivity nếu người dùng là admin
                        startActivity(Intent(this@LoginActivity, HomeAdminActivity::class.java))
                        finish()
                    } else {
                        promptForPassword(email)
                    }
                } else {
                    promptForPassword(email)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@LoginActivity, "Error fetching user role: $e", Toast.LENGTH_SHORT).show()
            }
    }

    // Nhắc người dùng nhập mật khẩu và xử lý đăng nhập
    private fun promptForPassword(email: String) {
        val password = inputPassword.text.toString()

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@LoginActivity) { task ->
                progressBar.visibility = View.GONE

                if (!task.isSuccessful) {
                    if (password.length < 6) {
                        inputPassword.error = getString(R.string.minimum_password)
                    } else {
                        Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                    }
                } else {
                    startActivity(Intent(this@LoginActivity, DifficultySelectionActivity::class.java))
                    finish()
                    saveLoginData(email)
                }
            }
    }

    // Lưu dữ liệu đăng nhập sau khi đăng nhập thành công
    private fun saveLoginData(email: String) {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val user = hashMapOf(
            "e-mail" to email,
            "role" to "user",
            "start time" to currentDateTime.format(formatter),
            "end time" to currentDateTime.format(formatter)
        )

        db.collection("login")
            .add(user)
            .addOnSuccessListener {
                Toast.makeText(this@LoginActivity, "Login data saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@LoginActivity, "Error saving login data: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
