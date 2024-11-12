package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class QuizTryhardActivity: AppCompatActivity() {

  lateinit var tvQuestion: TextView
  lateinit var btnAnswer1: Button
  lateinit var btnAnswer2: Button
  lateinit var btnAnswer3: Button
  lateinit var btnAnswer4: Button
  private lateinit var ivPlayerImage: ImageView
  private lateinit var progressBarTimer: ProgressBar


  private lateinit var players: List<QuizzModel>
  private var currentQuestionDifficulty: String = "easy"
  private lateinit var correctAnswer: String
  private var score = 0
  private var questionIndex = 0
  private var timer: CountDownTimer? = null
  private var quizStartTime: Long = 0L

  private val difficultyOrder = listOf("easy", "medium", "hard").shuffled() // Random order of difficulties

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    quizStartTime = System.currentTimeMillis()
    setContentView(R.layout.activity_quiz_tryhard)

    tvQuestion = findViewById(R.id.tvQuestion)
    btnAnswer1 = findViewById(R.id.btnAnswer1)
    btnAnswer2 = findViewById(R.id.btnAnswer2)
    btnAnswer3 = findViewById(R.id.btnAnswer3)
    btnAnswer4 = findViewById(R.id.btnAnswer4)
    ivPlayerImage = findViewById(R.id.ivPlayerImage)
    progressBarTimer = findViewById(R.id.progressBarTimer)

    val firebaseRepo = FirebaseRepository()
    firebaseRepo.getPlayersData { playerList ->
      players = playerList
      setupQuiz()
    }

    setupAnswerButtons()
  }

  private fun setupQuiz() {
    // Rotate through "easy", "medium", "hard" for each question
    val currentDifficulty = difficultyOrder[questionIndex % difficultyOrder.size]
    val player = players.random()
    currentQuestionDifficulty = currentDifficulty
    Glide.with(this).load(player.imageUrl).into(ivPlayerImage)

    when (currentDifficulty) {
      "easy" -> {
        tvQuestion.text = "Cầu thủ ${player.name} này thuộc câu lạc bộ nào?"
        correctAnswer = player.club
        setupAnswerOptions(player.club, player.name, player.yearOfBirth.toString())
      }
      "medium" -> {
        tvQuestion.text = "Cầu thủ này tên gì?"
        correctAnswer = player.name
        setupAnswerOptions(player.name, player.club, player.yearOfBirth.toString())
      }
      "hard" -> {
        tvQuestion.text = "Cầu thủ ${player.name} này sinh năm bao nhiêu?"
        correctAnswer = player.yearOfBirth.toString()
        setupAnswerOptions(player.yearOfBirth.toString(), player.club, player.name)
      }
    }

    questionIndex++
    startCountdownTimer()
  }

  private fun setupAnswerOptions(correct: String, wrong1: String, wrong2: String) {
    val wrongAnswers = when (currentQuestionDifficulty) {
      "easy" -> players.filter { it.club != correct }.map { it.club }  // Filter clubs
      "medium" -> players.filter { it.name != correct }.map { it.name }  // Filter names
      "hard" -> players.filter { it.yearOfBirth.toString() != correct }.map { it.yearOfBirth.toString() }  // Filter birth years
      else -> emptyList()
    }.shuffled().take(3) // Take 3 unique wrong answers

    // Add the correct answer and shuffle
    val answers = mutableListOf(correct).apply { addAll(wrongAnswers) }.shuffled()

    btnAnswer1.text = answers[0]
    btnAnswer2.text = answers[1]
    btnAnswer3.text = answers[2]
    btnAnswer4.text = answers[3]
  }

  private fun setupAnswerButtons() {
    val clickListener = View.OnClickListener { view ->
      timer?.cancel()
      val selectedAnswer = (view as Button).text.toString()

      if (selectedAnswer == correctAnswer) {
        score += 90
        tvQuestion.text = "Đúng rồi! +90 điểm"
        view.postDelayed({
          setupQuiz() // Next question
        }, 2000)
      } else {
        navigateToScoreActivity() // End the game
      }
    }

    btnAnswer1.setOnClickListener(clickListener)
    btnAnswer2.setOnClickListener(clickListener)
    btnAnswer3.setOnClickListener(clickListener)
    btnAnswer4.setOnClickListener(clickListener)
  }

  private fun startCountdownTimer() {
    progressBarTimer.max = 8
    timer = object : CountDownTimer(8000, 1000) {
      override fun onTick(millisUntilFinished: Long) {
        progressBarTimer.progress = (millisUntilFinished / 1000).toInt()
      }
      override fun onFinish() {
        navigateToScoreActivity() // End the game
      }
    }.start()
  }

  private fun navigateToScoreActivity() {
    val quizEndTime = System.currentTimeMillis()
    val timeTakenInMillis = quizEndTime - quizStartTime
    val intent = Intent(this, ScoreActivity::class.java)
    intent.putExtra("SCORE", score)
    intent.putExtra("DIFFICULTY", "VeryHard") // Set "tryhard" as the difficulty
    intent.putExtra("TIME_TAKEN", timeTakenInMillis)
    startActivity(intent)
    finish()
  }
}
