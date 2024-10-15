package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.material.bottomnavigation.BottomNavigationView

class RankingActivity : AppCompatActivity() {

    private lateinit var firstPlaceName: TextView
    private lateinit var secondPlaceName: TextView
    private lateinit var thirdPlaceName: TextView
    private lateinit var rankingListLayout: LinearLayout

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        // Kết nối với các view trong layout
        firstPlaceName = findViewById(R.id.first_place_name)
        secondPlaceName = findViewById(R.id.second_place_name)
        thirdPlaceName = findViewById(R.id.third_place_name)
        rankingListLayout = findViewById(R.id.ranking_list)

        // Thiết lập BottomNavigationView
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
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
                else -> false
            }
        }

        // Lấy dữ liệu từ Firestore
        loadRankingData()
    }

    private fun loadRankingData() {
        // Lấy danh sách điểm từ Firestore, sắp xếp theo điểm giảm dần
        db.collection("score")
            .orderBy("point", Query.Direction.DESCENDING)
            .limit(10) // Lấy tối đa 10 người chơi
            .get()
            .addOnSuccessListener { result ->
                val rankingListItems = mutableListOf<String>()  // Để chứa các người chơi từ vị trí 4 trở đi

                var count = 0
                for (document in result) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPoint = document.getString("point") ?: "0"

                    count++
                    when (count) {
                        1 -> firstPlaceName.text = "$playerName - $playerPoint pts"
                        2 -> secondPlaceName.text = "$playerName - $playerPoint pts"
                        3 -> thirdPlaceName.text = "$playerName - $playerPoint pts"
                        else -> {
                            // Thêm người chơi từ vị trí 4 trở đi vào danh sách
                            val playerTextView = TextView(this)
                            playerTextView.text = "$count. $playerName - $playerPoint pts"
                            playerTextView.setPadding(16, 16, 16, 16)
                            rankingListLayout.addView(playerTextView)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load ranking: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
