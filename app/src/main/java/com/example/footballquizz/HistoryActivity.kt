package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SearchView
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
  private lateinit var searchView: SearchView
  private lateinit var btnSort: ImageButton
  private var historyList = mutableListOf<ScoreModel>()

  // Boolean to track sort order (ascending by default)
  private var isAscending = true
  private var currentSortCriteria = "name"  // Default sorting by name

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history)

    listViewHistory = findViewById(R.id.listViewHistory)
    searchView = findViewById(R.id.searchView)
    btnSort = findViewById(R.id.btnSort)
    setupBottomNavigation()

    val userEmail = intent.getStringExtra("USER_EMAIL") ?: auth.currentUser?.email
    if (userEmail == null) {
      Toast.makeText(this, "User email not provided!", Toast.LENGTH_SHORT).show()
      return
    }

    fetchHistoryData(userEmail)

    // Setup search functionality
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean {
        historyAdapter.filter.filter(query)
        return false
      }

      override fun onQueryTextChange(newText: String?): Boolean {
        historyAdapter.filter.filter(newText)
        return false
      }
    })

    // Setup sort menu
    btnSort.setOnClickListener { showSortMenu() }
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
        historyList.clear()
        for (document in documents) {
          val scoreModel = ScoreModel(
            name = document.getString("name") ?: "",
            score = document.getString("point") ?: "",
            difficulty = document.getString("difficulty") ?: "",
            timeTaken = document.getString("timeTaken") ?: "",
            time = document.getString("time") ?: "",
            date = document.getString("date") ?: ""
          )
          historyList.add(scoreModel)
        }

        historyAdapter = HistoryAdapter(this, historyList)
        listViewHistory.adapter = historyAdapter
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Failed to fetch history: $e", Toast.LENGTH_SHORT).show()
      }
  }

  private fun showSortMenu() {
    val popupMenu = androidx.appcompat.widget.PopupMenu(this, btnSort)
    val inflater: MenuInflater = popupMenu.menuInflater
    inflater.inflate(R.menu.menu_sort, popupMenu.menu)
    popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
      when (menuItem.itemId) {
        R.id.sort_name -> {
          currentSortCriteria = "name"
          sortHistory()
        }
        R.id.sort_score -> {
          currentSortCriteria = "score"
          sortHistory()
        }
        R.id.sort_date -> {
          currentSortCriteria = "date"
          sortHistory()
        }
        R.id.sort_difficulty -> {
          currentSortCriteria = "difficulty"
          sortHistory()
        }
        R.id.sort_time -> {
          currentSortCriteria = "time"
          sortHistory()
        }
        R.id.sort_ascending -> {
          isAscending = true
          sortHistory()
        }
        R.id.sort_descending -> {
          isAscending = false
          sortHistory()
        }
      }
      true
    }
    popupMenu.show()
  }

  private fun sortHistory() {
    historyAdapter.sortList(currentSortCriteria, isAscending)
    historyAdapter.notifyDataSetChanged()
  }
}
