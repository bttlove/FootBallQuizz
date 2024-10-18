package com.example.footballquizz
<<<<<<< HEAD

=======
>>>>>>> a49484782f86951e03bf795a32292ce749418be0
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
<<<<<<< HEAD

=======
>>>>>>> a49484782f86951e03bf795a32292ce749418be0
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
        db.collection("score")
            .get() // Không sắp xếp từ Firestore vì Firestore đang lưu dưới dạng chuỗi
            .addOnSuccessListener { result ->
                val rankingListItems = mutableListOf<Pair<String, Double>>()  // List lưu tên người chơi và điểm số (dạng số)

                for (document in result) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0 // Chuyển điểm số từ chuỗi sang số

                    rankingListItems.add(Pair(playerName, playerPoint)) // Thêm vào danh sách dưới dạng cặp (tên, điểm số)
                }

                // Sắp xếp danh sách theo điểm số (từ cao xuống thấp)
                rankingListItems.sortByDescending { it.second }

                // Hiển thị top 3 người chơi
                if (rankingListItems.isNotEmpty()) firstPlaceName.text = "${rankingListItems[0].first} - ${rankingListItems[0].second} pts"
                if (rankingListItems.size > 1) secondPlaceName.text = "${rankingListItems[1].first} - ${rankingListItems[1].second} pts"
                if (rankingListItems.size > 2) thirdPlaceName.text = "${rankingListItems[2].first} - ${rankingListItems[2].second} pts"

                // Hiển thị những người chơi còn lại
                for (i in 3 until rankingListItems.size) {
                    val playerTextView = TextView(this)
                    playerTextView.text = "${i + 1}. ${rankingListItems[i].first} - ${rankingListItems[i].second} pts"
                    playerTextView.setPadding(16, 16, 16, 16)
                    rankingListLayout.addView(playerTextView)
                }
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load ranking: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
