package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class AdminRankingActivity : AppCompatActivity() {

    private lateinit var searchPlayerRankingAdminEditText: EditText
    private lateinit var addPlayerRankingAdminButton: Button
    private lateinit var firstPlaceName: TextView
    private lateinit var secondPlaceName: TextView
    private lateinit var thirdPlaceName: TextView
    private lateinit var firstPlaceImage_admin: ImageView
    private lateinit var secondPlaceImage_admin: ImageView
    private lateinit var thirdPlaceImage_admin: ImageView
    private lateinit var rankingListLayout: LinearLayout
    private lateinit var nextPageButton: Button
    private lateinit var previousPageButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var currentPage = 0
    private val itemsPerPage = 10
    private var rankingListItems: MutableList<Pair<String, Double>> = mutableListOf()
    private lateinit var pageNumberTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking_admin)

        searchPlayerRankingAdminEditText = findViewById(R.id.searchPlayerRankingAdminEditText)
        addPlayerRankingAdminButton = findViewById(R.id.addPlayerRankingAdminButton)
        firstPlaceName = findViewById(R.id.first_place_name)
        secondPlaceName = findViewById(R.id.second_place_name)
        thirdPlaceName = findViewById(R.id.third_place_name)
        firstPlaceImage_admin = findViewById(R.id.first_place_image)
        secondPlaceImage_admin = findViewById(R.id.second_place_image)
        thirdPlaceImage_admin = findViewById(R.id.third_place_image)
        rankingListLayout = findViewById(R.id.ranking_list)
        nextPageButton = findViewById(R.id.nextPageButton)
        previousPageButton = findViewById(R.id.prevPageButton)
        pageNumberTextView = findViewById(R.id.pageNumberTextView)

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
                R.id.nav_settings_admin -> {
                    startActivity(Intent(this, AdminSettingActivity::class.java))
                    true
                }
                else -> false
            }
        }


        searchPlayerRankingAdminEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
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
                    loadRankingData()
                }
            }
        })

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
                val searchResults: MutableList<Pair<String, Double>> = mutableListOf()

                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    searchResults.add(Pair(playerName, playerPoint))
                }

                rankingListItems = rankingListItems.take(3).toMutableList()
                rankingListItems.addAll(searchResults.sortedByDescending { it.second })

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
                val searchResults: MutableList<Pair<String, Double>> = mutableListOf()

                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0

                    searchResults.add(Pair(playerName, playerPoint))
                }

                rankingListItems = rankingListItems.take(3).toMutableList()
                rankingListItems.addAll(searchResults.sortedByDescending { it.second })

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
                val searchResults: MutableList<Pair<String, Double>> = mutableListOf()

                for (document in scoreResult) {
                    val playerName = document.getString("name") ?: "No Name"
                    val playerPointString = document.getString("point") ?: "0"
                    val playerPoint = playerPointString.toDoubleOrNull() ?: 0.0
                    if (playerPoint == score) {
                        searchResults.add(Pair(playerName, playerPoint))
                    }
                }

                rankingListItems = rankingListItems.take(3).toMutableList()
                rankingListItems.addAll(searchResults.sortedByDescending { it.second })
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
        textView.text = "$playerName - $playerPoints points"

        val imageView = when (index) {
            0 -> firstPlaceImage_admin
            1 -> secondPlaceImage_admin
            2 -> thirdPlaceImage_admin
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
                        val intent = Intent(this@AdminRankingActivity, Profile_AdminActivity::class.java)
                        intent.putExtra("USER_EMAIL", email)
                        startActivity(intent)
                    }
                }
            }
    }

    private fun updateRankingUI() {
        rankingListLayout.removeAllViews()
        if (rankingListItems.isNotEmpty()) firstPlaceName.text = "${rankingListItems[0].first} - ${rankingListItems[0].second}"
        if (rankingListItems.size > 1) secondPlaceName.text = "${rankingListItems[1].first} - ${rankingListItems[1].second} "
        if (rankingListItems.size > 2) thirdPlaceName.text = "${rankingListItems[2].first} - ${rankingListItems[2].second} "
        val startIndex = currentPage * itemsPerPage + 3
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
        previousPageButton.isEnabled = currentPage > 0
        nextPageButton.isEnabled = endIndex < rankingListItems.size
    }
}