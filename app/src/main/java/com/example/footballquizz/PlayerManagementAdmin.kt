package com.example.footballquizz

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
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
            .orderBy("start time", com.google.firebase.firestore.Query.Direction.DESCENDING)  // Sắp xếp theo 'start time' giảm dần
            .limit(10)  // Giới hạn 10 kết quả mới nhất
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val email = document.getString("e-mail") ?: "No Email"  // Lấy 'email'
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
                        text = email
                    }

                    val startTimeTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = formattedLoginTime
                    }

                    val endTimeTextView = TextView(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = formattedLogoutTime
                    }

                    // Thêm Button "Chặn người chơi"
                    val blockButton = Button(this).apply {
                        layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        text = "Chặn"
                        setOnClickListener {
                            showBlockDialog(email)  // Hiển thị dialog chặn người chơi
                        }
                    }

                    // Thêm các TextView và Button vào TableRow
                    playerRow.addView(emailTextView)
                    playerRow.addView(startTimeTextView)
                    playerRow.addView(endTimeTextView)
                    playerRow.addView(blockButton)

                    // Thêm TableRow vào TableLayout
                    val playerListLayout: TableLayout = findViewById(R.id.player_list)
                    playerListLayout.addView(playerRow)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không thể tải dữ liệu người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }


    // Hiển thị dialog để chặn người chơi
    private fun showBlockDialog(email: String) {
        val options = arrayOf("Chặn 7 ngày", "Chặn 14 ngày")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn thời gian chặn cho $email")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> blockPlayer(email, 7)  // Chặn 7 ngày
                1 -> blockPlayer(email, 14) // Chặn 14 ngày
            }
        }
        builder.show()
    }

    // Xử lý logic chặn người chơi
    private fun blockPlayer(email: String, days: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val unblockDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Lưu thời gian mở khóa vào Firestore
        db.collection("login").document(email)
            .update("block_until", unblockDate)
            .addOnSuccessListener {
                Toast.makeText(this, "Người chơi $email đã bị chặn trong $days ngày.", Toast.LENGTH_SHORT).show()

                // Cập nhật nút "Chặn" thành "Đã chặn" ngay khi chặn thành công
                updateBlockButton(email)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi chặn người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }




    private fun updateBlockButton(email: String) {
        val playerListLayout: TableLayout = findViewById(R.id.player_list)

        for (i in 0 until playerListLayout.childCount) {
            val row = playerListLayout.getChildAt(i) as TableRow
            val emailTextView = row.getChildAt(0) as TextView  // TextView chứa email
            val blockButton = row.getChildAt(3) as Button      // Button "Chặn"

            // So sánh email của người dùng hiện tại với email cần chặn
            if (emailTextView.text == email) {
                blockButton.text = "Đã chặn"
                blockButton.isEnabled = false  // Vô hiệu hóa nút để không thể bấm tiếp
                Toast.makeText(this, "$email đã bị chặn", Toast.LENGTH_SHORT).show()  // Thêm thông báo cho người dùng
                break
            }
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
