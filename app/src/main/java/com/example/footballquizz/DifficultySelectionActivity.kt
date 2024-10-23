package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DifficultySelectionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

        // Lấy instance của FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Lấy người dùng hiện tại
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Lấy email của người dùng
            val email = currentUser.email

            if (email != null) {
                // Tách phần tên trước dấu @
                val username = email.split("@")[0]

                // Hiển thị tên người dùng trong phần chào mừng
                val greetingTextView: TextView = findViewById(R.id.greeting)
                greetingTextView.text = "Hello, $username!"

                // Lấy ảnh đại diện từ Firestore
                val userId = currentUser.uid
                val db = FirebaseFirestore.getInstance()

                // Lấy ảnh đại diện từ collection "auths"
                db.collection("auths").document(userId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val imageUrl = document.getString("image_url")
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            val profileImageView: ImageView = findViewById(R.id.profile_icon)
                            // Sử dụng Glide để load ảnh
                            Glide.with(this).load(imageUrl).into(profileImageView)
                        }
                    }
                }

                // Lấy điểm cao nhất và tính rank
                db.collection("score").get().addOnSuccessListener { result ->
                    val scoresList = mutableListOf<Pair<String, Int>>() // Danh sách chứa email và điểm
                    for (document in result) {
                        val userEmail = document.getString("e-mail")
                        val points = document.getString("point")?.toIntOrNull() ?: 0
                        if (userEmail != null) {
                            scoresList.add(Pair(userEmail, points))
                        }
                    }

                    // Sắp xếp danh sách theo điểm giảm dần
                    scoresList.sortByDescending { it.second }

                    // Tìm điểm cao nhất của người dùng và tính rank
                    var highestPoints = 0
                    var rank = 0
                    for ((index, pair) in scoresList.withIndex()) {
                        if (pair.first == email) {
                            highestPoints = pair.second
                            rank = index + 1 // Thứ hạng bắt đầu từ 1
                            break
                        }
                    }

                    // Hiển thị rank và điểm cao nhất lên giao diện
                    val rankTextView: TextView = findViewById(R.id.rank_textview)
                    val pointsTextView: TextView = findViewById(R.id.points_textview)

                    rankTextView.text = "Rank: $rank"
                    pointsTextView.text = "Points: $highestPoints"
                }
            }
        }

        // Thiết lập thanh điều hướng
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Thiết lập nút chọn độ khó
        findViewById<Button>(R.id.btnEasy).setOnClickListener {
            startQuizActivity("easy")
        }

        findViewById<Button>(R.id.btnMedium).setOnClickListener {
            startQuizActivity("medium")
        }

        findViewById<Button>(R.id.btnHard).setOnClickListener {
            startQuizActivity("hard")
        }
    }

    private fun startQuizActivity(difficulty: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("DIFFICULTY", difficulty)
        startActivity(intent)
    }
}
