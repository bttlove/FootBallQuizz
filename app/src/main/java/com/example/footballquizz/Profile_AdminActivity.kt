package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class Profile_AdminActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var listViewHistory: ListView
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList = mutableListOf<ScoreModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_admin)

        listViewHistory =
            findViewById(R.id.listViewHistory) // Ensure you have this ListView in activity_profile.xml

        // Get the email passed from the PlayerManagementAdmin activity
        val userEmail = intent.getStringExtra("USER_EMAIL")

        if (userEmail != null) {
            // Fetch scores from Firestore for the given user email
            db.collection("score")
                .whereEqualTo("e-mail", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val score = document.getString("point") ?: ""
                        val name = document.getString("name") ?: ""
                        val difficulty = document.getString("difficulty") ?: ""
                        val timeTaken = document.getString("timeTaken") ?: ""
                        val time = document.getString("time") ?: ""

                        val scoreModel = ScoreModel(
                            name = name,
                            score = score,
                            difficulty = difficulty,
                            timeTaken = timeTaken,
                            time = time
                        )
                        historyList.add(scoreModel)
                    }

                    // Set up adapter
                    historyAdapter = HistoryAdapter(this, historyList)
                    listViewHistory.adapter = historyAdapter
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch history: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
        }
        // Set up bottom navigation
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

                R.id.nav_ranking_admin -> {
                    startActivity(Intent(this, AdminRankingActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
