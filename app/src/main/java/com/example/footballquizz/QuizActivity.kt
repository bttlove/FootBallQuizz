package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.os.CountDownTimer
import android.widget.ProgressBar

open class QuizActivity : AppCompatActivity() {

  lateinit var tvQuestion: TextView
  lateinit var btnAnswer1: Button
  lateinit var btnAnswer2: Button
  lateinit var btnAnswer3: Button
  lateinit var btnAnswer4: Button
  private lateinit var ivPlayerImage: ImageView
  private lateinit var tvTimer: TextView

  private var easyQuestionsAsked = 0
  private var mediumQuestionsAsked = 0
  private var hardQuestionsAsked = 0
  private var correctAnswers = 0
  private var incorrectAnswers = 0
  private var timer: CountDownTimer? = null

  private lateinit var players: List<QuizzModel>
  lateinit var correctAnswer: String
  private var difficulty: String? = null
  private var questionIndex = 0
  private var difficultyMix: List<String> = listOf()

  private var quizStartTime: Long = 0L
  private var remainingTimeInMillis: Long = 0L
  private var currentQuestionDifficulty: String = "easy"
  private var score: Int = 0
  private var isPracticeMode = false
  private lateinit var progressBarTimer: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_quiz)
    quizStartTime = System.currentTimeMillis()

    tvQuestion = findViewById(R.id.tvQuestion)
    btnAnswer1 = findViewById(R.id.btnAnswer1)
    btnAnswer2 = findViewById(R.id.btnAnswer2)
    btnAnswer3 = findViewById(R.id.btnAnswer3)
    btnAnswer4 = findViewById(R.id.btnAnswer4)
    ivPlayerImage = findViewById(R.id.ivPlayerImage)
    progressBarTimer = findViewById(R.id.progressBarTimer)
    progressBarTimer.progress = 100

    difficulty = intent.getStringExtra("DIFFICULTY")
    isPracticeMode = difficulty == "practice"

    difficultyMix = if (difficulty == "random") {
      generateRandomDifficultyMix()
    } else {
      List(10) { difficulty ?: "easy" }
    }

    val firebaseRepo = FirebaseRepository()
    firebaseRepo.getPlayersData { playerList ->
      players = playerList
      setupQuiz()
    }

    setupAnswerButtons()
  }

  private fun generateRandomDifficultyMix(): List<String> {
    val difficulties = mutableListOf<String>()
    val easyCount = (2..4).random()
    val hardCount = (2..4).random()
    val mediumCount = 10 - easyCount - hardCount

    repeat(easyCount) { difficulties.add("easy") }
    repeat(mediumCount) { difficulties.add("medium") }
    repeat(hardCount) { difficulties.add("hard") }

    return difficulties.shuffled()
  }

  fun setupQuiz() {
    if (questionIndex >= difficultyMix.size) {
      if (!isPracticeMode) {
        navigateToScoreActivity()
      } else {
        questionIndex = 0
      }
      return
    }

    when (difficulty) {
      "easy" -> if (++easyQuestionsAsked > 10) navigateToScoreActivity()
      "medium" -> if (++mediumQuestionsAsked > 10) navigateToScoreActivity()
      "hard" -> if (++hardQuestionsAsked > 10) navigateToScoreActivity()
    }

    val currentDifficulty = difficultyMix[questionIndex]
    currentQuestionDifficulty = currentDifficulty
    questionIndex++

    val player = players.random()
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

    if (!isPracticeMode) {
      startCountdownTimer()
    } else {
      tvTimer.visibility = View.GONE
    }
  }

  private fun setupAnswerOptions(correct: String, wrong1: String, wrong2: String) {
    // Select relevant wrong answers based on the current question's difficulty
    val wrongAnswers = when (currentQuestionDifficulty) {
      "easy" -> players.filter { it.club != correct }.map { it.club }  // Filter clubs
      "medium" -> players.filter { it.name != correct }.map { it.name }  // Filter names
      "hard" -> players.filter { it.yearOfBirth.toString() != correct }.map { it.yearOfBirth.toString() }  // Filter birth years
      else -> emptyList()
    }.shuffled().take(3)  // Get 3 unique wrong answers

    // Add the correct answer and shuffle
    val answers = mutableListOf(correct).apply { addAll(wrongAnswers) }.shuffled()

    // Set answers to buttons
    btnAnswer1.text = answers[0]
    btnAnswer2.text = answers[1]
    btnAnswer3.text = answers[2]
    btnAnswer4.text = answers[3]
  }


  private fun setupAnswerButtons() {
    val clickListener = View.OnClickListener { view ->
      if (!isPracticeMode) timer?.cancel()

      val selectedAnswer = (view as Button).text.toString()
      if (selectedAnswer == correctAnswer) {
        tvQuestion.text = "Đúng rồi!"
        if (btnAnswer1.text == correctAnswer) btnAnswer1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer2.text == correctAnswer) btnAnswer2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer3.text == correctAnswer) btnAnswer3.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer4.text == correctAnswer) btnAnswer4.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))

        if (!isPracticeMode) {
          correctAnswers++
          score += calculateScore()
          displayScore()
        }
      } else {
        tvQuestion.text = "Sai rồi, đáp án đúng là: $correctAnswer"
        view.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))

        if (!isPracticeMode) incorrectAnswers++
        if (btnAnswer1.text == correctAnswer) btnAnswer1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer2.text == correctAnswer) btnAnswer2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer3.text == correctAnswer) btnAnswer3.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer4.text == correctAnswer) btnAnswer4.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
      }

      view.postDelayed({
        resetAnswerButtonColors()
        setupQuiz()
      }, 2000)
    }

    btnAnswer1.setOnClickListener(clickListener)
    btnAnswer2.setOnClickListener(clickListener)
    btnAnswer3.setOnClickListener(clickListener)
    btnAnswer4.setOnClickListener(clickListener)
  }
  fun resetAnswerButtonColors() {
    btnAnswer1.setBackgroundColor(resources.getColor(R.color.bg_main))
    btnAnswer2.setBackgroundColor(resources.getColor(R.color.bg_main))
    btnAnswer3.setBackgroundColor(resources.getColor(R.color.bg_main))
    btnAnswer4.setBackgroundColor(resources.getColor(R.color.bg_main))
  }

  private fun displayScore() {
    if (!isPracticeMode) {
      Log.d("QuizActivity", "Total Score: $score")
      tvQuestion.append("\nCâu đúng: $correctAnswers\nCâu sai: $incorrectAnswers\nĐiểm hiện tại: $score")
    }
  }

  private fun calculateScore(): Int {
    return when (currentQuestionDifficulty) {
      "easy" -> 10
      "medium" -> 50
      "hard" -> 100
      else -> 0
    }
  }

  private fun startCountdownTimer() {
    val totalTime = 15000L // 20 seconds in milliseconds
    progressBarTimer.max = (totalTime / 1000).toInt()
    remainingTimeInMillis = totalTime
    timer = object : CountDownTimer(totalTime, 1000) {
      override fun onTick(millisUntilFinished: Long) {
        remainingTimeInMillis = millisUntilFinished
        val secondsRemaining = (millisUntilFinished / 1000).toInt() // Calculate full seconds remaining
        progressBarTimer.progress = secondsRemaining // Set progress in seconds
      }

      override fun onFinish() {
        tvQuestion.text = "Hết thời gian! Đáp án đúng là: $correctAnswer"
        progressBarTimer.progress = 0
        incorrectAnswers++
        displayScore()
        setupQuiz()
      }
    }.start()
  }

  private fun navigateToScoreActivity() {
    val quizEndTime = System.currentTimeMillis()
    val timeTakenInMillis = quizEndTime - quizStartTime
    val intent = Intent(this, ScoreActivity::class.java)
    intent.putExtra("SCORE", score)
    intent.putExtra("DIFFICULTY", difficulty)
    intent.putExtra("TIME_TAKEN", timeTakenInMillis)
    startActivity(intent)
    finish()
  }

}
