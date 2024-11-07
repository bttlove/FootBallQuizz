package com.example.footballquizz
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class AdminSettingActivity : AppCompatActivity(){
    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvBoD: TextView
    private lateinit var tvPosition: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_admin)

        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvBoD = findViewById(R.id.tvBoD)
        tvPosition = findViewById(R.id.tvPosition)

        fetchUserData()
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (userEmail != null) {
                db.collection("users")
                    .whereEqualTo("e-mail", userEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        for (document in documents) {
                            val name = document.getString("name")
                            val email = document.getString("e-mail")
                            val phone = document.getString("Phone")
                            val bod = document.getString("BoD")
                            val role = document.getString("role")
                            val imageUrl = document.getString("image_url")

                            tvName.text = "Họ và Tên: $name"
                            tvEmail.text = "E-mail: $email"
                            tvPhone.text = "Số điện thoại: $phone"
                            tvBoD.text = "BoD: $bod"
                            tvPosition.text = "Vị trí: $role"

                            // Load the profile picture with Glide
                            Glide.with(this)
                                .load(imageUrl)
                                .into(imgProfilePicture)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Lỗi khi lấy dữ liệu: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Email người dùng không xác định", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }
}
