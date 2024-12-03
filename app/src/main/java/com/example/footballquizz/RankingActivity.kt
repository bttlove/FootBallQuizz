package com.example.footballquizz
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.Normalizer
import java.util.regex.Pattern

class RankingActivity : AppCompatActivity() {

    private lateinit var btnSort: ImageButton
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
    private var sortAscending = true
    private var currentSortField = "name"
    private var isSorting = false

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
        btnSort = findViewById(R.id.btnSort)

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

        val recyclerView: RecyclerView = findViewById(R.id.playersRecyclerView)
        val playerList = mutableListOf<Pair<String, Double>>()
        val adapter = RankingAdapter(playerList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadRankingData(adapter)

        btnSort.setOnClickListener {
            val popupMenu = PopupMenu(this, btnSort)
            popupMenu.menuInflater.inflate(R.menu.menu_ranking, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.sort_alphabetical_Ranking-> {
                        currentSortField = "name"
                        sortPlayerData("name")
                    }
                    R.id.sort_by_point_Ranking -> {
                        currentSortField = "point"
                        sortPlayerData("point")
                    }
                }
                true
            }
            popupMenu.show()
        }

        addPlayerRankingButton.setOnClickListener {
            isToastShown = false
            val query = searchPlayerRankingEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                // Tắt chế độ tìm kiếm và sắp xếp
                isSorting = false
                isSearching = true
                currentPage = 0
                searchResultsItems.clear()

                when {
                    query.contains("@") -> {
                        searchByEmail(query)
                    }
                    query.toDoubleOrNull() != null -> {
                        searchByScore(query.toDouble())
                    }
                    else -> {
                        searchByName(query)
                    }
                }
            } else {
                // Reset chế độ sắp xếp và tìm kiếm
                isSorting = false
                isSearching = false
                currentPage = 0
                rankingListItems = rankingListItems.sortedByDescending { it.second }.toMutableList()
                updateRankingUI()

            }
        }



        nextPageButton.setOnClickListener {
            if (currentPage < (if (isSearching) searchResultsItems else rankingListItems).size / itemsPerPage) {
                currentPage++
                updateRankingUI()
            }
        }

        previousPageButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateRankingUI()
            }
        }


        loadRankingData(adapter)
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
        // Chuẩn hóa chuỗi nhập để so sánh
        val normalizedQuery = normalizeString(name)

        db.collection("score")
            .get()
            .addOnSuccessListener { scoreResult ->
                searchResultsItems.clear() // Xóa kết quả cũ trước khi tìm kiếm mới
                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    // Chuẩn hóa tên người chơi trước khi so sánh
                    val normalizedPlayerName = normalizeString(playerName)
                    if (normalizedPlayerName.contains(normalizedQuery, ignoreCase = true)) {
                        searchResultsItems.add(Pair(playerName, playerPoint))
                    }
                }
                updateRankingUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Không tìm thấy người chơi: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun normalizeString(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(normalized).replaceAll("").lowercase()
    }

    private fun searchByScore(score: Double) {
        searchResultsItems.clear()
        db.collection("score")
            .get()
            .addOnSuccessListener { querySnapshot ->
                var foundResults = false

                for (document in querySnapshot) {
                    val playerName = document.getString("name") ?: "No Name"
                    val pointString = document.getString("point") ?: "0"


                    val playerPoint = try {
                        pointString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.0
                    }


                    if (playerPoint == score) {
                        searchResultsItems.add(Pair(playerName, playerPoint))
                        foundResults = true
                    }
                }


                isSearching = true
                if (foundResults) {
                    Toast.makeText(this, "Tìm thấy ${searchResultsItems.size} người chơi có điểm $score", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Không tìm thấy người chơi có điểm $score", Toast.LENGTH_SHORT).show()
                }

                updateRankingUI()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi tìm kiếm: $e", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadRankingData(adapter: RankingAdapter) {
        db.collection("score")
            .get()
            .addOnSuccessListener { querySnapshot ->
                rankingListItems.clear()

                for (document in querySnapshot) {
                    val playerName = document.getString("name") ?: "No Name"
                    val pointString = document.getString("point") ?: "0"
                    val playerPoint = try {
                        pointString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.0
                    }
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

        textView.text = "$playerName $playerPoints "

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
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.hinh1)
                .into(it)

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

    private fun sortPlayerData(sortField: String) {
        isSorting = true

        // Tách Top 1, 2, 3 ra khỏi danh sách
        val topThree = if (rankingListItems.size > 3) rankingListItems.take(3) else rankingListItems
        val restOfList = if (rankingListItems.size > 3) rankingListItems.drop(3) else listOf<Pair<String, Double>>()

        // Thực hiện sắp xếp cho phần còn lại
        when (sortField) {
            "name" -> {
                if (sortAscending) {
                    rankingListItems = restOfList.sortedWith { p1, p2 ->
                        val name1 = p1.first
                        val name2 = p2.first
                        val isName1Digit = name1[0].isDigit()
                        val isName2Digit = name2[0].isDigit()

                        when {
                            isName1Digit && !isName2Digit -> 1
                            !isName1Digit && isName2Digit -> -1
                            else -> name1.lowercase().compareTo(name2.lowercase())
                        }
                    }.toMutableList()
                } else {
                    rankingListItems = restOfList.sortedWith { p1, p2 ->
                        val name1 = p1.first
                        val name2 = p2.first
                        val isName1Digit = name1[0].isDigit()
                        val isName2Digit = name2[0].isDigit()

                        when {
                            isName1Digit && !isName2Digit -> 1
                            !isName1Digit && isName2Digit -> -1
                            else -> name2.lowercase().compareTo(name1.lowercase())
                        }
                    }.toMutableList()
                }
            }
            "point" -> {
                if (sortAscending) {
                    rankingListItems = restOfList.sortedBy { it.second }.toMutableList()
                } else {
                    rankingListItems = restOfList.sortedByDescending { it.second }.toMutableList()
                }
            }
        }

        // Đưa lại Top 1, 2, 3 vào đầu danh sách
        rankingListItems = (topThree + rankingListItems).toMutableList()

        // Đánh dấu lại trạng thái sắp xếp
        isSorting = true

        // Reset currentPage về 0
        currentPage = 0

        // Cập nhật lại UI sau khi sắp xếp
        updateRankingUI()

        // Đổi trạng thái sắp xếp tăng/giảm
        sortAscending = !sortAscending
    }

    private fun getRealRank(player: Pair<String, Double>): Int {
        return rankingListItems.indexOf(player) + 1 // Tìm vị trí và cộng 1 (thứ hạng bắt đầu từ 1)
    }

    private var isToastShown = false

    private fun updateRankingUI() {
        rankingListLayout.removeAllViews()
        if (rankingListItems.isNotEmpty()) {
            firstPlaceName.text = "${rankingListItems[0].first} - ${rankingListItems[0].second}"
            if (rankingListItems.size > 1) secondPlaceName.text = "${rankingListItems[1].first} - ${rankingListItems[1].second}"
            if (rankingListItems.size > 2) thirdPlaceName.text = "${rankingListItems[2].first} - ${rankingListItems[2].second}"
        }

        val itemsToDisplay = if (isSearching) searchResultsItems else rankingListItems

        pageNumberTextView.text = "Page ${currentPage + 1}"

        val startIndex = currentPage * itemsPerPage + if (!isSearching) 3 else 0
        val endIndex = minOf(startIndex + itemsPerPage, itemsToDisplay.size)

        val adjustedItems = itemsToDisplay.subList(startIndex, endIndex)
        for ((index, item) in adjustedItems.withIndex()) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            // Tính globalRank dựa trên trạng thái isSorting
            val globalRank = if (isSorting) index + 1 + currentPage * itemsPerPage else startIndex + index + 1

            val actualRank = if (isSearching) {
                getRealRank(item) // Lấy thứ hạng thực tế của người chơi
            } else {
                globalRank // Nếu không tìm kiếm, sử dụng thứ hạng tính toán dựa trên phân trang
            }

            val rankTextView = TextView(this)
            rankTextView.text = actualRank.toString()
            rankTextView.setPadding(8, 8, 8, 8)
            rankTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            rankTextView.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
            rankTextView.gravity = Gravity.START

            val playerNameTextView = TextView(this)
            playerNameTextView.text = item.first
            playerNameTextView.setPadding(8, 8, 8, 8)
            playerNameTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            val playerPointTextView = TextView(this)
            playerPointTextView.text = item.second.toString()
            playerPointTextView.setPadding(8, 8, 8, 8)
            playerPointTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            playerPointTextView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END

            tableRow.addView(rankTextView)
            tableRow.addView(playerNameTextView)
            tableRow.addView(playerPointTextView)

            rankingListLayout.addView(tableRow)
        }

        previousPageButton.isEnabled = currentPage > 0
        nextPageButton.isEnabled = endIndex < itemsToDisplay.size
    }
}


