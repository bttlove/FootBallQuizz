package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

  private lateinit var listViewHistory: ListView
  private lateinit var historyAdapter: HistoryAdapter
  private var historyList = mutableListOf<ScoreModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history)

    listViewHistory = findViewById(R.id.listViewHistory)
    setupBottomNavigation()

    val userEmail = intent.getStringExtra("USER_EMAIL") ?: auth.currentUser?.email
    if (userEmail == null) {
      Toast.makeText(this, "User email not provided!", Toast.LENGTH_SHORT).show()
      return
    }

    fetchHistoryData(userEmail)
  }

  private fun setupBottomNavigation() {
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
        R.id.nav_ranking -> {
          startActivity(Intent(this, RankingActivity::class.java))
          true
        }
        else -> false
      }
    }
  }

  private fun fetchHistoryData(userEmail: String) {
    db.collection("score")
      .whereEqualTo("e-mail", userEmail)
      .get()
      .addOnSuccessListener { documents ->
        historyList.clear() // Clear existing data if any
        for (document in documents) {
          val scoreModel = ScoreModel(
            name = document.getString("name") ?: "",
            score = document.getString("point") ?: "",
            difficulty = document.getString("difficulty") ?: "",
            timeTaken = document.getString("timeTaken") ?: "",
            time = document.getString("time") ?: ""
          )
          historyList.add(scoreModel)
        }

        // Initialize the ArrayAdapter with the fetched data
        historyAdapter = HistoryAdapter(this, historyList)
        listViewHistory.adapter = historyAdapter
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Failed to fetch history: $e", Toast.LENGTH_SHORT).show()
      }
  }
}
