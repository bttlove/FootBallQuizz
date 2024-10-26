package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class RankingActivity : AppCompatActivity() {

    private lateinit var firstPlaceName: TextView
    private lateinit var secondPlaceName: TextView
    private lateinit var thirdPlaceName: TextView
    private lateinit var rankingListLayout: LinearLayout
    private lateinit var nextPageButton: Button
    private lateinit var previousPageButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var currentPage = 0
    private val itemsPerPage = 10
    private var rankingListItems: MutableList<Pair<String, Double>> = mutableListOf() // Thay đổi đây
    private lateinit var pageNumberTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        // Kết nối với các view trong layout
        firstPlaceName = findViewById(R.id.first_place_name)
        secondPlaceName = findViewById(R.id.second_place_name)
        thirdPlaceName = findViewById(R.id.third_place_name)
        rankingListLayout = findViewById(R.id.ranking_list)
        nextPageButton = findViewById(R.id.nextPageButton) // Thêm nút trang tiếp theo
        previousPageButton = findViewById(R.id.prevPageButton) // Thêm nút trang trước
        pageNumberTextView = findViewById(R.id.pageNumberTextView)

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

        // Thiết lập sự kiện cho các nút điều hướng trang
        nextPageButton.setOnClickListener {
            currentPage++
            loadRankingData()
        }

        previousPageButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                loadRankingData()
            }
        }

        // Lấy dữ liệu từ Firestore
        loadRankingData()
    }

    private fun loadRankingData() {
        db.collection("score")
            .get()
            .addOnSuccessListener { result ->
                rankingListItems.clear() // Xóa danh sách hiện tại trước khi thêm mới

                for (document in result) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    rankingListItems.add(Pair(playerName, playerPoint))
                }

                rankingListItems.sortByDescending { it.second }
                updateRankingUI()
                pageNumberTextView.text = "Page $currentPage"
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load ranking: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRankingUI() {
        rankingListLayout.removeAllViews() // Xóa các hàng hiện tại trước khi cập nhật

        // Hiển thị top 3 người chơi
        if (rankingListItems.isNotEmpty()) firstPlaceName.text = "${rankingListItems[0].first} - ${rankingListItems[0].second} point"
        if (rankingListItems.size > 1) secondPlaceName.text = "${rankingListItems[1].first} - ${rankingListItems[1].second} point"
        if (rankingListItems.size > 2) thirdPlaceName.text = "${rankingListItems[2].first} - ${rankingListItems[2].second} point"

        // Tính toán chỉ số bắt đầu và kết thúc cho trang hiện tại
        val startIndex = currentPage * itemsPerPage + 3 // Bắt đầu từ mục thứ 4
        val endIndex = minOf(startIndex + itemsPerPage, rankingListItems.size)

        for (i in startIndex until endIndex) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            val playerNameTextView = TextView(this)
            playerNameTextView.text = rankingListItems[i].first
            playerNameTextView.setPadding(8, 8, 8, 8)
            playerNameTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            val playerPointTextView = TextView(this)
            playerPointTextView.text = rankingListItems[i].second.toString()
            playerPointTextView.setPadding(8, 8, 8, 8)
            playerPointTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            playerPointTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END

            tableRow.addView(playerNameTextView)
            tableRow.addView(playerPointTextView)

            rankingListLayout.addView(tableRow)
        }

        // Ẩn hoặc hiện nút "Previous" và "Next" tùy thuộc vào trang hiện tại
        previousPageButton.isEnabled = currentPage > 0
        nextPageButton.isEnabled = endIndex < rankingListItems.size
    }
}
