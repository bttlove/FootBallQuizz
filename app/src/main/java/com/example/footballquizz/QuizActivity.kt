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

class QuizActivity : AppCompatActivity() {

  private lateinit var tvQuestion: TextView
  private lateinit var btnAnswer1: Button
  private lateinit var btnAnswer2: Button
  private lateinit var btnAnswer3: Button
  private lateinit var btnAnswer4: Button
  private lateinit var ivPlayerImage: ImageView
  private lateinit var tvTimer: TextView
  private var easyQuestionsAsked = 0
  private var easyCorrectAnswers = 0
  private var mediumCorrectAnswers = 0
  private var hardCorrectAnswers = 0
  private var mediumQuestionsAsked = 0
  private var hardQuestionsAsked = 0
  private var correctAnswers = 0
  private var incorrectAnswers = 0
  private var timer: CountDownTimer? = null

  private lateinit var players: List<QuizzModel>
  private lateinit var correctAnswer: String
  private var difficulty: String? = null

  private var quizStartTime: Long = 0L
  private var remainingTimeInMillis: Long = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_quiz)

    // Initialize views
    tvQuestion = findViewById(R.id.tvQuestion)
    btnAnswer1 = findViewById(R.id.btnAnswer1)
    btnAnswer2 = findViewById(R.id.btnAnswer2)
    btnAnswer3 = findViewById(R.id.btnAnswer3)
    btnAnswer4 = findViewById(R.id.btnAnswer4)
    ivPlayerImage = findViewById(R.id.ivPlayerImage)
    tvTimer = findViewById(R.id.tvTimer)

    difficulty = intent.getStringExtra("DIFFICULTY")

    // Start time of the quiz
    quizStartTime = System.currentTimeMillis()

    // Get player data from Firebase
    val firebaseRepo = FirebaseRepository()
    firebaseRepo.getPlayersData { playerList ->
      players = playerList
      setupQuiz()
    }

    setupAnswerButtons()
  }

  // Setup quiz question
  private fun setupQuiz() {
    if (players.isEmpty()) {
      tvQuestion.text = "Không có dữ liệu cầu thủ!"
      return
    }

    // Reset timer if any
    timer?.cancel()

    // Check the number of questions asked per difficulty
    when (difficulty) {
      "easy" -> {
        if (easyQuestionsAsked >= 10) {
          navigateToScoreActivity()
          return
        }
        easyQuestionsAsked++
      }
      "medium" -> {
        if (mediumQuestionsAsked >= 10) {
          navigateToScoreActivity()
          return
        }
        mediumQuestionsAsked++
      }
      "hard" -> {
        if (hardQuestionsAsked >= 10) {
          navigateToScoreActivity()
          return
        }
        hardQuestionsAsked++
      }
    }

    // Randomly select a player
    val player = players.random()

    // Display player's image
    Glide.with(this).load(player.imageUrl).into(ivPlayerImage)

    // Create question and answers based on difficulty
    when (difficulty) {
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

    // Start countdown timer
    startCountdownTimer()
  }

  // Setup answer options
  private fun setupAnswerOptions(correct: String, wrong1: String, wrong2: String) {
    val answers = mutableListOf(correct)
    val wrongAnswers = players.filter {
      it.name != correct &&
              it.club != wrong1 &&
              it.yearOfBirth.toString() != wrong2
    }.shuffled().take(3)

    answers.addAll(wrongAnswers.map {
      when (difficulty) {
        "easy" -> it.club
        "medium" -> it.name
        "hard" -> it.yearOfBirth.toString()
        else -> ""
      }
    })

    answers.shuffle()

    // Assign answers to buttons
    btnAnswer1.text = answers[0]
    btnAnswer2.text = answers[1]
    btnAnswer3.text = answers[2]
    btnAnswer4.text = answers[3]
  }

  // Setup answer buttons
  private fun setupAnswerButtons() {
    val clickListener = View.OnClickListener { view ->
      timer?.cancel()

      val selectedAnswer = (view as Button).text.toString()
      if (selectedAnswer == correctAnswer) {
        tvQuestion.text = "Đúng rồi!"
        correctAnswers++

        when (difficulty) {
          "easy" -> easyCorrectAnswers++
          "medium" -> mediumCorrectAnswers++
          "hard" -> hardCorrectAnswers++
        }
      } else {
        tvQuestion.text = "Sai rồi, đáp án đúng là: $correctAnswer"
        incorrectAnswers++
      }

      // Display score
      displayScore()

      // Load next question after 1 second
      view.postDelayed({
        setupQuiz()
      }, 1000)
    }

    // Assign click listeners
    btnAnswer1.setOnClickListener(clickListener)
    btnAnswer2.setOnClickListener(clickListener)
    btnAnswer3.setOnClickListener(clickListener)
    btnAnswer4.setOnClickListener(clickListener)
  }

  // Display score
  private fun displayScore() {
    val score = calculateScore()
    tvQuestion.append("\nCâu đúng: $correctAnswers\nCâu sai: $incorrectAnswers\nĐiểm: $score")
  }

  // Calculate score based on difficulty
  private fun calculateScore(): Int {
    var score = 0
    score += easyCorrectAnswers * 1
    score += mediumCorrectAnswers * 5
    score += hardCorrectAnswers * 10
    return score
  }

  // Start countdown timer
  private fun startCountdownTimer() {
    timer = object : CountDownTimer(30000, 1000) { // 30 seconds per question
      override fun onTick(millisUntilFinished: Long) {
        remainingTimeInMillis = millisUntilFinished
        val secondsRemaining = millisUntilFinished / 1000
        tvTimer.text = "Thời gian còn lại: $secondsRemaining giây"
      }

      override fun onFinish() {
        remainingTimeInMillis = 0
        tvQuestion.text = "Hết thời gian! Đáp án đúng là: $correctAnswer"
        incorrectAnswers++

        // Display score
        displayScore()

        // Load next question after 1 second
        tvQuestion.postDelayed({
          setupQuiz()
        }, 1000)
      }
    }.start()
  }

  // Navigate to ScoreActivity
  private fun navigateToScoreActivity() {
    timer?.cancel()

    val quizEndTime = System.currentTimeMillis()
    val totalTimeTaken = quizEndTime - quizStartTime

    val intent = Intent(this, ScoreActivity::class.java)
    intent.putExtra("SCORE", calculateScore())
    intent.putExtra("DIFFICULTY", difficulty)
    intent.putExtra("TIME_TAKEN", totalTimeTaken)
    startActivity(intent)
    finish()
  }
}
