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
            // Lấy email người dùng hiện tại từ FirebaseAuth
            val currentEmail = auth.currentUser?.email
            if (currentEmail != null) {
                // Kiểm tra vai trò người dùng trong Firestore
                checkUserRole(currentEmail)
            } else {
                // Nếu không lấy được email, yêu cầu đăng nhập lại
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
        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString()

            // Kiểm tra nếu email bị bỏ trống
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra xem người dùng có phải admin không bằng cách tra trong Firestore
            checkUserRole(email)
        }
    }

    // Kiểm tra vai trò của người dùng
    private fun checkUserRole(email: String) {
        db.collection("user").document(email).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        // Nếu là admin, chuyển người dùng đến AdminActivity
                        startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                        finish() // Đóng LoginActivity
                    } else {
                        // Nếu không phải admin, kiểm tra đăng nhập với Authentication như người dùng bình thường
                        promptForPassword(email)
                    }
                } else {
                    // Nếu không tìm thấy trong Firestore, xem như người dùng bình thường
                    promptForPassword(email)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@LoginActivity, "Error fetching user role: $e", Toast.LENGTH_SHORT).show()
            }
    }

    // Xử lý đăng nhập người dùng thông thường
    private fun promptForPassword(email: String) {
        val password = inputPassword.text.toString()

        // Kiểm tra nếu mật khẩu bị bỏ trống
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        // Đăng nhập với Firebase Authentication cho người dùng bình thường
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@LoginActivity) { task ->
                progressBar.visibility = View.GONE

                if (!task.isSuccessful) {
                    // Nếu đăng nhập thất bại
                    if (password.length < 6) {
                        inputPassword.error = getString(R.string.minimum_password)
                    } else {
                        Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Đăng nhập thành công, điều hướng đến DifficultySelectionActivity
                    startActivity(Intent(this@LoginActivity, DifficultySelectionActivity::class.java))
                    finish() // Đóng LoginActivity
                    saveLoginData(email) // Lưu thông tin đăng nhập
                }
            }
    }

    // Lưu thông tin đăng nhập vào Firestore
    private fun saveLoginData(email: String) {
        // Lấy thời gian hiện tại
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Tạo dữ liệu đăng nhập
        val user = hashMapOf(
            "e-mail" to email,
            "role" to "user", // Có thể cần cập nhật đúng role thực sự nếu cần
            "start time" to currentDateTime.format(formatter),
            "end time" to currentDateTime.format(formatter) // Sử dụng cùng thời gian cho lúc bắt đầu, có thể thay đổi sau
        )

        // Thêm thông tin đăng nhập vào Firestore
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
