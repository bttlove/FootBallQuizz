package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DifficultySelectionActivity : AppCompatActivity() {

  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_difficulty_selection)

    auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser != null) {
      val email = currentUser.email
      if (email != null) {
        val username = email.split("@")[0]
        val greetingTextView: TextView = findViewById(R.id.greeting)
        greetingTextView.text = "Hello, $username"

        val userId = currentUser.uid
        val db = FirebaseFirestore.getInstance()
        db.collection("auths").document(userId).get().addOnSuccessListener { document ->
          val imageUrl = document.getString("image_url")

          if (imageUrl != null && imageUrl.isNotEmpty()) {
            val profileImageView: ImageView = findViewById(R.id.profile_icon)

            // Tạo RequestOptions với circleCrop và kích thước tùy chỉnh
            val requestOptions = RequestOptions().circleCrop().override(150, 150)

            // Sử dụng Glide với RequestOptions
            Glide.with(this)
              .load(imageUrl)
              .apply(requestOptions)
              .into(profileImageView)
          }
        }

        db.collection("score").get().addOnSuccessListener { result ->
          val scoresList = mutableListOf<Pair<String, Int>>()
          for (document in result) {
            val userEmail = document.getString("e-mail")
            val points = document.getString("point")?.toIntOrNull() ?: 0
            if (userEmail != null) {
              scoresList.add(Pair(userEmail, points))
            }
          }
          scoresList.sortByDescending { it.second }
          var highestPoints = 0
          var rank = 0
          for ((index, pair) in scoresList.withIndex()) {
            if (pair.first == email) {
              highestPoints = pair.second
              rank = index + 1
              break
            }
          }
          findViewById<TextView>(R.id.rank_textview).text = "Rank: $rank"
          findViewById<TextView>(R.id.points_textview).text = "Points: $highestPoints"
        }
      }
    }

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

    findViewById<Button>(R.id.btnEasy).setOnClickListener {
      startQuizActivity("easy")
    }

    findViewById<Button>(R.id.btnMedium).setOnClickListener {
      startQuizActivity("medium")
    }

    findViewById<Button>(R.id.btnHard).setOnClickListener {
      startQuizActivity("hard")
    }

    // Random selection button for difficulty
    findViewById<Button>(R.id.btnRandom).setOnClickListener {
      startQuizActivity("random")
    }

    // Practice mode button
    findViewById<Button>(R.id.btnPractice).setOnClickListener {
      startPracticeActivity()
    }
    findViewById<Button>(R.id.btnTryHard).setOnClickListener {
      startTryHardActivity()
    }
  }

  private fun startQuizActivity(difficulty: String) {
    val intent = Intent(this, QuizActivity::class.java)
    intent.putExtra("DIFFICULTY", difficulty)
    startActivity(intent)
  }

  private fun startPracticeActivity() {
    val intent = Intent(this, QuizPracticeActivity::class.java)
    startActivity(intent)
  }
  private fun startTryHardActivity() {
    val intent = Intent(this, QuizTryhardActivity::class.java)
    startActivity(intent)
  }
}
