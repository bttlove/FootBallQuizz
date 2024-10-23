package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
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
                R.id.nav_home_admin -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Lấy dữ liệu từ Firestore
        loadPlayerData()
    }

    private fun loadPlayerData() {
        db.collection("login")  // Collection tên 'login'
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val playerEmail = document.getString("email") ?: "No Email"  // Lấy 'email'
                    val startTime = document.getString("start time") ?: "Unknown"
                    val endTime = document.getString("end time") ?: "Unknown"

                    // Định dạng thời gian
                    val formattedLoginTime = formatDateTime(startTime)
                    val formattedLogoutTime = formatDateTime(endTime)

                    // Tạo một TableRow mới cho mỗi người chơi
                    val playerRow = TableRow(this)

                    // Tạo TextView cho từng cột
                    val emailTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = playerEmail
                        var padding = 8
                    }

                    val startTimeTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = formattedLoginTime

                    }

                    val endTimeTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = formattedLogoutTime

                    }

                    // Thêm các TextView vào TableRow
                    playerRow.addView(emailTextView)
                    playerRow.addView(startTimeTextView)
                    playerRow.addView(endTimeTextView)

                    // Thêm TableRow vào TableLayout
                    val playerListLayout: TableLayout = findViewById(R.id.player_list)
                    playerListLayout.addView(playerRow)
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
