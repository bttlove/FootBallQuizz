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
  private lateinit var btnViewHistory: Button

  private val db = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_score)

    tvScore = findViewById(R.id.tvScore)
    playerNameEditText = findViewById(R.id.playerNameEditText)
    btnSubmitScore = findViewById(R.id.btnSubmitScore)
    btnRestart = findViewById(R.id.btnRestart)
    btnViewHistory = findViewById(R.id.btnViewHistory)

    // Receive data from intent
    val score = intent.getIntExtra("SCORE", 0)
    val difficulty = intent.getStringExtra("DIFFICULTY")
    val timeTakenInMillis = intent.getLongExtra("TIME_TAKEN", 0L)

    val minutes = (timeTakenInMillis / 1000) / 60
    val seconds = (timeTakenInMillis / 1000) % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    tvScore.text = "Tổng điểm của bạn: $score\nThời gian hoàn thành: $timeFormatted"
    //tvScore.text = "Tổng điểm của bạn: $score\nThời gian hoàn thành: $timeTakenInMillis"

    // Get the current logged-in user's email
    val currentUser = auth.currentUser
    val userEmail = currentUser?.email

    if (userEmail == null) {
      Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show()
      return
    }

    // Handle score submission to Firestore
    btnSubmitScore.setOnClickListener {
      val playerName = playerNameEditText.text.toString()

      if (playerName.isEmpty()) {
        Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      // Check that difficulty is not null
      if (difficulty == null) {
        Toast.makeText(this, "Difficulty level is missing!", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }

      // Get the current time
      val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
      val currentTime = sdf.format(Date())

      // Create a map to store in Firestore
      val scoreData = hashMapOf(
        "e-mail" to userEmail,
        "name" to playerName,
        "point" to score.toString(),
        "difficulty" to difficulty,
        "timeTaken" to timeFormatted,
        "time" to currentTime,
        "date" to SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
      )

      // Push data to Firestore in the "score" collection
      db.collection("score")
        .add(scoreData)
        .addOnSuccessListener {
          Toast.makeText(this, "Lưu điểm thành công!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
          Toast.makeText(this, "Lưu điểm thất bại: $e", Toast.LENGTH_SHORT).show()
        }
    }

    // Restart game
    btnRestart.setOnClickListener {
      val intent = Intent(this, DifficultySelectionActivity::class.java)
      startActivity(intent)
      finish()
    }

    // View history
    btnViewHistory.setOnClickListener {
      val intent = Intent(this, HistoryActivity::class.java)
      startActivity(intent)
    }
  }
}
