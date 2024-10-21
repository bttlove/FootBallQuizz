package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class PlayerManagementAdmin : AppCompatActivity() {

    private lateinit var playerListLayout: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_management_admin)

        // Kết nối với các view trong layout
        playerListLayout = findViewById(R.id.player_list)

        // Thiết lập BottomNavigationView
        val bottomNavigation: BottomNavigationView = findViewById(R.id.admin_bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DifficultySelectionActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_home_admin -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    true
                }
                R.id.nav_player_management -> {
                    // Nếu đã ở trong PlayerManagementAdmin, không làm gì cả
                    if (this is PlayerManagementAdmin) {
                        return@setOnItemSelectedListener true
                    }
                    startActivity(Intent(this, PlayerManagementAdmin::class.java))
                    true
                }
                else -> false
            }
        }

        // Lấy dữ liệu từ Firestore
        loadPlayerData()
    }

    private fun loadPlayerData() {
        db.collection("players")  // Giả sử collection của bạn có tên là 'players'
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val playerName = document.getString("name") ?: "No Name"
                    val loginTime = document.getString("loginTime") ?: "Unknown"
                    val logoutTime = document.getString("logoutTime") ?: "Unknown"

                    // Định dạng thời gian
                    val formattedLoginTime = formatDateTime(loginTime)
                    val formattedLogoutTime = formatDateTime(logoutTime)

                    // Tạo một TextView mới cho mỗi người chơi
                    val playerTextView = TextView(this)
                    playerTextView.text = "$playerName: Đăng nhập lúc $formattedLoginTime, Đăng xuất lúc $formattedLogoutTime"
                    playerTextView.setPadding(16, 16, 16, 16)
                    playerListLayout.addView(playerTextView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không thể tải dữ liệu người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDateTime(dateTime: String): String {
        return try {
            // Chuyển đổi chuỗi datetime sang định dạng
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = originalFormat.parse(dateTime)
            val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            targetFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}
