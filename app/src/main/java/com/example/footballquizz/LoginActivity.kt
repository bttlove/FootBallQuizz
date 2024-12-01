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

      progressBar.visibility = View.VISIBLE
      auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this@LoginActivity) { task ->
          progressBar.visibility = View.GONE
          if (task.isSuccessful) {
            checkUserRole(email) // Gọi hàm kiểm tra vai trò người dùng
          } else {
            Toast.makeText(this@LoginActivity, "Authentication failed!", Toast.LENGTH_LONG).show()
          }
        }
    }
  }

  // Kiểm tra vai trò người dùng
  private fun checkUserRole(email: String) {
    val sanitizedEmail = email.trim().lowercase()

    // Kiểm tra role trực tiếp trong collection `user`
    db.collection("user").document(sanitizedEmail).get()
      .addOnSuccessListener { document ->
        if (document != null && document.exists()) {
          val role = document.getString("role") ?: "user"
          if (role == "admin") {
            // Chuyển hướng đến trang Admin
            startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
            finish()
          } else if (role == "Banned") {
            val banReason = document.getString("banReason")
            val bannedAt = document.getString("bannedAt")
            val banEndAt = document.getString("banEndAt")

            // Xử lý logic thời gian cấm
            handleBanStatus(sanitizedEmail, banReason, bannedAt, banEndAt)
          } else {
            // Cho phép đăng nhập nếu không bị cấm
            promptForPassword(email)
          }
        } else {
          promptForPassword(email)
          //Toast.makeText(this, "Không tìm thấy người dùng.", Toast.LENGTH_SHORT).show()
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Lỗi kiểm tra vai trò: $e", Toast.LENGTH_SHORT).show()
      }
  }

  private fun handleBanStatus(
    email: String,
    banReason: String?,
    bannedAt: String?,
    banEndAt: String?
  ) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDate = Date()

    try {
      if (banEndAt != null) {
        val banEndDate = sdf.parse(banEndAt)

        if (banEndDate != null && currentDate.before(banEndDate)) {
          // Người dùng vẫn đang bị cấm
          showBanDialog(banReason, bannedAt, banEndAt)
        } else {
          // Hết thời gian cấm, cập nhật lại vai trò và xóa khỏi `user`
          db.collection("auths").document(email).update(
            mapOf(
              "role" to "user",
              "banReason" to null,
              "bannedAt" to null,
              "banEndAt" to null
            )
          ).addOnSuccessListener {
            db.collection("user").document(email).delete()
              .addOnSuccessListener {
                Toast.makeText(this, "Cấm hết hạn, bạn có thể đăng nhập.", Toast.LENGTH_SHORT).show()
                promptForPassword(email)
              }
              .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi xóa dữ liệu cấm: $e", Toast.LENGTH_SHORT).show()
              }
          }.addOnFailureListener { e ->
            Toast.makeText(this, "Lỗi cập nhật role: $e", Toast.LENGTH_SHORT).show()
          }
        }
      } else {
        Toast.makeText(this, "Thiếu thông tin thời gian cấm.", Toast.LENGTH_SHORT).show()
      }
    } catch (e: Exception) {
      Toast.makeText(this, "Lỗi định dạng thời gian: $e", Toast.LENGTH_SHORT).show()
    }
  }

  private fun showBanDialog(banReason: String?, bannedAt: String?, banEndAt: String?) {
    val dialogView = layoutInflater.inflate(R.layout.dialog_ban_info, null)

    val tvBanReason: TextView = dialogView.findViewById(R.id.tv_ban_reason)
    val tvBanAt: TextView = dialogView.findViewById(R.id.tv_ban_at)
    val tvBanUntil: TextView = dialogView.findViewById(R.id.tv_ban_until)
    val btnClose: Button = dialogView.findViewById(R.id.btn_close)

    tvBanReason.text = "Reason: ${banReason ?: "Unknown"}"
    tvBanAt.text = "Banned at: ${bannedAt ?: "Unknown"}"
    tvBanUntil.text = "Ban until: ${banEndAt ?: "Unknown"}"

    val dialog = android.app.AlertDialog.Builder(this)
      .setView(dialogView)
      .setCancelable(false)
      .create()

    btnClose.setOnClickListener {
      dialog.dismiss()
      auth.signOut()
    }

    dialog.show()
  }

  // Hàm đăng nhập sau khi kiểm tra trạng thái
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
          // Đăng nhập thành công, chuyển đến giao diện tiếp theo
          startActivity(Intent(this@LoginActivity, DifficultySelectionActivity::class.java))
          finish()
          saveLoginData(email)
        }
      }
  }

  // Lưu thông tin đăng nhập vào Firestore
  private fun saveLoginData(email: String) {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val user = hashMapOf(
      "e-mail" to email,
      "role" to "user",
      "start time" to currentDateTime.format(formatter),
      "end time" to currentDateTime.format(formatter)
    )

    db.collection("login").document(email)
      .set(user)
      .addOnSuccessListener {
        Toast.makeText(this, "Login data saved.", Toast.LENGTH_SHORT).show()
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Error saving login data: $e", Toast.LENGTH_SHORT).show()
      }
  }
}
