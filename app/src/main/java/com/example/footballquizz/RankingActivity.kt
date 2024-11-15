
package com.example.footballquizz
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView


class RankingActivity : AppCompatActivity() {

    private lateinit var searchPlayerRankingEditText: EditText
    private lateinit var addPlayerRankingButton: Button
    private lateinit var firstPlaceName: TextView
    private lateinit var secondPlaceName: TextView
    private lateinit var thirdPlaceName: TextView
    private lateinit var firstPlaceImage: ImageView
    private lateinit var secondPlaceImage: ImageView
    private lateinit var thirdPlaceImage: ImageView
    private lateinit var rankingListLayout: LinearLayout
    private lateinit var nextPageButton: Button
    private lateinit var previousPageButton: Button
    private val db = FirebaseFirestore.getInstance()
    private var currentPage = 0
    private val itemsPerPage = 10
    private var rankingListItems: MutableList<Pair<String, Double>> = mutableListOf() // Thay đổi đây
    private lateinit var pageNumberTextView: TextView
    private var searchResultsItems: MutableList<Pair<String, Double>> = mutableListOf() // Lưu trữ kết quả tìm kiếm
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        searchPlayerRankingEditText = findViewById(R.id.searchPlayerRankingEditText)
        addPlayerRankingButton = findViewById(R.id.addPlayerRankingButton)
        firstPlaceName = findViewById(R.id.first_place_name)
        secondPlaceName = findViewById(R.id.second_place_name)
        thirdPlaceName = findViewById(R.id.third_place_name)
        firstPlaceImage = findViewById(R.id.first_place_image)
        secondPlaceImage = findViewById(R.id.second_place_image)
        thirdPlaceImage = findViewById(R.id.third_place_image)
        rankingListLayout = findViewById(R.id.ranking_list)
        nextPageButton = findViewById(R.id.nextPageButton)
        previousPageButton = findViewById(R.id.prevPageButton)
        pageNumberTextView = findViewById(R.id.pageNumberTextView)
        
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
                else -> false
            }
        }

        addPlayerRankingButton.setOnClickListener {
            val query = searchPlayerRankingEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                // Kích hoạt trạng thái tìm kiếm và đặt lại trang hiện tại về 0
                isSearching = true
                currentPage = 0
                searchResultsItems.clear()

                when {
                    query.contains("@") -> {
                        searchByEmail(query) // Tìm theo email
                    }
                    query.toDoubleOrNull() != null -> {
                        searchByScore(query.toDouble()) // Tìm theo điểm
                    }
                    else -> {
                        searchByName(query) // Tìm theo tên
                    }
                }
            } else {
                // Không có nội dung trong thanh tìm kiếm, hiển thị toàn bộ dữ liệu
                isSearching = false
                currentPage = 0
                loadRankingData()
            }
        }


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

        loadRankingData()
    }

    private fun searchByEmail(email: String) {
        db.collection("score")
            .whereEqualTo("e-mail", email)
            .get()
            .addOnSuccessListener { scoreResult ->
                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    searchResultsItems.add(Pair(playerName, playerPoint))
                }
                updateRankingUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không tìm thấy người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchByName(name: String) {
        db.collection("score")
            .whereGreaterThanOrEqualTo("name", name)
            .whereLessThanOrEqualTo("name", name + '\uf8ff')
            .get()
            .addOnSuccessListener { scoreResult ->
                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    searchResultsItems.add(Pair(playerName, playerPoint))
                }
                updateRankingUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không tìm thấy người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchByScore(score: Double) {
        db.collection("score")
            .get()
            .addOnSuccessListener { scoreResult ->
                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0
                    if (playerPoint == score) {
                        searchResultsItems.add(Pair(playerName, playerPoint))
                    }
                }
                updateRankingUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không tìm thấy người chơi với điểm $score: $e", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadRankingData() {
        db.collection("score")
            .get()
            .addOnSuccessListener { result ->
                rankingListItems.clear()

                for (document in result) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    rankingListItems.add(Pair(playerName, playerPoint))
                }

                rankingListItems.sortByDescending { it.second }
                updateTop3PlayersWithImages()
                updateRankingUI()
                pageNumberTextView.text = "Page $currentPage"
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load ranking: $e", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateTop3PlayersWithImages() {
        for (i in 0 until minOf(3, rankingListItems.size)) {
            val playerName = rankingListItems[i].first
            fetchImageUrlForPlayer(playerName, i)
        }
    }

    private fun fetchImageUrlForPlayer(playerName: String, index: Int) {
        db.collection("score")
            .whereEqualTo("name", playerName)
            .get()
            .addOnSuccessListener { scoreResult ->
                if (scoreResult.documents.isNotEmpty()) {
                    val email = scoreResult.documents[0].getString("e-mail")
                    if (email != null) {
                        db.collection("auths")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { authResult ->
                                if (!authResult.isEmpty) {
                                    val imageUrl = authResult.documents[0].getString("image_url")
                                    updateTopPlayerView(index, playerName, rankingListItems[index].second, imageUrl)
                                }
                            }
                            .addOnFailureListener {
                            }
                    }
                }
            }
    }

    private fun updateTopPlayerView(index: Int, playerName: String, playerPoints: Double, imageUrl: String?) {
        val textView = when (index) {
            0 -> firstPlaceName
            1 -> secondPlaceName
            2 -> thirdPlaceName
            else -> return
        }
        textView.text = "$playerName - $playerPoints "

        val imageView = when (index) {
            0 -> firstPlaceImage
            1 -> secondPlaceImage
            2 -> thirdPlaceImage
            else -> null
        }

        textView.setOnClickListener {
            navigateToProfile(playerName)
        }


        imageView?.setOnClickListener {
            navigateToProfile(playerName)
        }

        imageView?.let {
            Glide.with(this).load(imageUrl).into(it)
        }
    }

    private fun navigateToProfile(playerName: String) {
        db.collection("score")
            .whereEqualTo("name", playerName)
            .get()
            .addOnSuccessListener { scoreResult ->
                if (scoreResult.documents.isNotEmpty()) {
                    val email = scoreResult.documents[0].getString("e-mail") // Lấy email từ kết quả
                    if (email != null) {
                        val intent = Intent(this@RankingActivity, HistoryActivity::class.java)
                        intent.putExtra("USER_EMAIL", email)
                        startActivity(intent)
                    }
                }
            }
    }

    private fun updateRankingUI() {
        rankingListLayout.removeAllViews()

        // Luôn lấy top 1, 2, 3 từ danh sách đầy đủ (rankingListItems)
        if (rankingListItems.isNotEmpty()) {
            firstPlaceName.text = "${rankingListItems[0].first} - ${rankingListItems[0].second}"
            if (rankingListItems.size > 1) secondPlaceName.text = "${rankingListItems[1].first} - ${rankingListItems[1].second}"
            if (rankingListItems.size > 2) thirdPlaceName.text = "${rankingListItems[2].first} - ${rankingListItems[2].second}"
        }

        // Xác định danh sách hiển thị dựa trên chế độ tìm kiếm
        val itemsToDisplay = if (isSearching) searchResultsItems else rankingListItems
        pageNumberTextView.text = "Page $currentPage"

        val startIndex = currentPage * itemsPerPage + 3 // Bắt đầu từ sau top 3
        val endIndex = minOf(startIndex + itemsPerPage, itemsToDisplay.size)

        for (i in startIndex until endIndex) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            val playerNameTextView = TextView(this)
            playerNameTextView.text = itemsToDisplay[i].first
            playerNameTextView.setPadding(8, 8, 8, 8)
            playerNameTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            val playerPointTextView = TextView(this)
            playerPointTextView.text = itemsToDisplay[i].second.toString()
            playerPointTextView.setPadding(8, 8, 8, 8)
            playerPointTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            playerPointTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END

            tableRow.addView(playerNameTextView)
            tableRow.addView(playerPointTextView)

            rankingListLayout.addView(tableRow)
        }

        previousPageButton.isEnabled = currentPage > 0
        nextPageButton.isEnabled = endIndex < itemsToDisplay.size
    }
}

