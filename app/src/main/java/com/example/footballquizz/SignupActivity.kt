package com.example.footballquizz


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    private lateinit var btnChooseImage: Button
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1
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
        btnChooseImage = findViewById(R.id.choose_image_button)
        imageView = findViewById(R.id.imageView)
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
        btnChooseImage.setOnClickListener {
            chooseImage()
        }
    }
    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri) // Hiển thị ảnh đã chọn
        }
    }
    // Tạo tài khoản và upload dữ liệu lên Firestore
    private fun createAccount() {
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        // Kiểm tra thông tin nhập vào
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

        // Tạo tài khoản trên Firebase Authentication
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

                    if (imageUri != null) {
                        // Upload ảnh lên Firebase Storage
                        uploadImageToStorage(userId, email, formattedDateTime)
                    } else {
                        saveUserDataToFirestore(userId, email, "", formattedDateTime)
                    }
                } else {
                    showToast("Authentication failed: ${task.exception?.message}")
                }
            }
    }
    // Upload ảnh lên Firebase Storage
    private fun uploadImageToStorage(userId: String?, email: String, dateTime: String) {
        if (userId == null) {
            showToast("Error: Could not get user ID.")
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference.child("profile_images/$userId")
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener {
                // Lấy URL ảnh sau khi upload
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveUserDataToFirestore(userId, email, imageUrl, dateTime)
                }
            }
            .addOnFailureListener { e ->
                showToast("Image upload failed: ${e.message}")
            }
    }
    private fun saveUserDataToFirestore(userId: String?, email: String, imageUrl: String, dateTime: String) {
        val db = FirebaseFirestore.getInstance()

        // Dữ liệu người dùng
        val userData = hashMapOf(
            "email" to email,
            "role" to "user",
            "date-time" to dateTime,

            "image_url" to imageUrl // URL ảnh
        )

        // Lưu vào Firestore
        db.collection("auths").document(userId!!)
            .set(userData)
            .addOnSuccessListener {
                showToast("Account created and saved successfully.")
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Failed to save user data: ${e.message}")
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
