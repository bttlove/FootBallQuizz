package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ScoreActivity : AppCompatActivity() {

  private lateinit var tvScore: TextView
  private lateinit var playerNameEditText: EditText
  private lateinit var btnSubmitScore: Button
  private lateinit var btnRestart: Button

  private val db = FirebaseFirestore.getInstance() // Firebase Firestore instance
  private val auth = FirebaseAuth.getInstance()   // FirebaseAuth instance

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_score)

    tvScore = findViewById(R.id.tvScore)
    playerNameEditText = findViewById(R.id.playerNameEditText)
    btnSubmitScore = findViewById(R.id.btnSubmitScore)
    btnRestart = findViewById(R.id.btnRestart)

    // Receive score from intent
    val score = intent.getIntExtra("SCORE", 0)
    tvScore.text = "Tổng điểm của bạn: $score"

    // Get the current logged-in user's email from FirebaseAuth
    val currentUser = auth.currentUser
    val userEmail = currentUser?.email

    if (userEmail == null) {
      Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show()
      return
    }

    // Handle score submission to Firebase
    btnSubmitScore.setOnClickListener {
      val playerName = playerNameEditText.text.toString()

      if (playerName.isEmpty()) {
        Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      // Get the current time
      val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
      val currentTime = sdf.format(Date())

      // Create a map to store in Firestore
      val scoreData = hashMapOf(
        "e-mail" to userEmail,  // Use logged-in user's email
        "name" to playerName,
        "point" to score.toString(),
        "time" to currentTime
      )

      // Push data to Firestore in the "score" collection
      db.collection("score")
        .add(scoreData)
        .addOnSuccessListener {
          Toast.makeText(this, "Score submitted successfully!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, "Failed to submit score: $e", Toast.LENGTH_SHORT).show()
        }
    }

    // Restart game
    btnRestart.setOnClickListener {
      val intent = Intent(this, DifficultySelectionActivity::class.java)
      startActivity(intent)
      finish() // Close ScoreActivity
    }
  }
}
