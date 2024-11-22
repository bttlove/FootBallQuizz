package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminSettingActivity : AppCompatActivity() {
    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvBoD: TextView
    private lateinit var tvPosition: TextView
    private lateinit var btnLogout: Button
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
        btnLogout = findViewById(R.id.btn_logout)
        fetchUserData()
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
            // Chuyển về màn hình đăng nhập
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation_admin)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home_admin -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    true
                }
                R.id.nav_player_management -> {
                    startActivity(Intent(this, PlayerManagementAdmin::class.java))
                    true
                }
                R.id.nav_ranking_admin -> {
                    startActivity(Intent(this, AdminRankingActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (userEmail != null) {
                db.collection("user")
                    .document(userEmail) // Truy cập trực tiếp vào tài liệu với userEmail
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val name = document.getString("name")
                            val email = document.getString("e-mail")
                            val phone = document.getString("Phone")
                            val bod = document.getString("BoD")
                            val role = document.getString("role")
                            val imageUrl = document.getString("image_url")

                            tvName.text = "Họ và Tên: $name"
                            tvEmail.text = "E-mail: $email"
                            tvPhone.text = "Số điện thoại: $phone"
                            tvBoD.text = "Ngày sinh: $bod"
                            tvPosition.text = "Vị trí: $role"

                            // Load ảnh đại diện bằng Glide
                            val requestOptions = RequestOptions().circleCrop().override(150, 150)
                            Glide.with(this)
                                .load(imageUrl)

                                .apply(requestOptions)
                                .into(imgProfilePicture)
                        } else {
                            Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show()
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
