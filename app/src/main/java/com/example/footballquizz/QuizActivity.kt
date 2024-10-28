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
  private var questionIndex = 0
  private var difficultyMix: List<String> = listOf()

  private var quizStartTime: Long = 0L
  private var remainingTimeInMillis: Long = 0L

  // Track actual difficulty of current question
  private var currentQuestionDifficulty: String = "easy"

  // Total score tracking
  private var totalScore: Int = 0 // New variable to hold the accumulated score

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
    difficultyMix = if (difficulty == "random") {
      generateRandomDifficultyMix() // Create mix for "random"
    } else {
      List(10) { difficulty ?: "easy" }
    }

    // Get player data from Firebase
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

  // Setup quiz question
  private fun setupQuiz() {
    if (questionIndex >= difficultyMix.size) {
      navigateToScoreActivity()
      return
    }

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

    val currentDifficulty = difficultyMix[questionIndex]
    currentQuestionDifficulty = currentDifficulty // Track actual question difficulty
    questionIndex++

    // Randomly select a player
    val player = players.random()

    // Display player's image
    Glide.with(this).load(player.imageUrl).into(ivPlayerImage)

    // Create question and answers based on difficulty
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

    // Start countdown timer for the question
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
      when (difficultyMix[questionIndex - 1]) {
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
        totalScore += calculateScore() // Add points for correct answer

        when (currentQuestionDifficulty) {
          "easy" -> easyCorrectAnswers++
          "medium" -> mediumCorrectAnswers++
          "hard" -> hardCorrectAnswers++
        }
        Log.d("QuizActivity", "Correct Answer! Total Correct: $correctAnswers, Score: ${calculateScore()}")
      } else {
        tvQuestion.text = "Sai rồi, đáp án đúng là: $correctAnswer"
        incorrectAnswers++
        Log.d("QuizActivity", "Incorrect Answer! Correct Answer was: $correctAnswer")
      }

      // Display score
      displayScore()

      // Automatically proceed to next question after 1 second
      view.postDelayed({
        setupQuiz()
      }, 1000)
    }

    // Assign click listener to answer buttons
    btnAnswer1.setOnClickListener(clickListener)
    btnAnswer2.setOnClickListener(clickListener)
    btnAnswer3.setOnClickListener(clickListener)
    btnAnswer4.setOnClickListener(clickListener)
  }

  // Display score
  private fun displayScore() {
    Log.d("QuizActivity", "Total Score: $totalScore") // Log total score
    tvQuestion.append("\nCâu đúng: $correctAnswers\nCâu sai: $incorrectAnswers\nĐiểm hiện tại: $totalScore")
  }

  // Calculate score based on difficulty
  private fun calculateScore(): Int {
    return when (currentQuestionDifficulty) {
      "easy" -> 10
      "medium" -> 50
      "hard" -> 100
      else -> 0
    }
  }

  // Start countdown timer for the question
  private fun startCountdownTimer() {
    remainingTimeInMillis = 20000 // 20 seconds
    tvTimer.text = formatTime(remainingTimeInMillis)

    timer = object : CountDownTimer(remainingTimeInMillis, 1000) {
      override fun onTick(millisUntilFinished: Long) {
        remainingTimeInMillis = millisUntilFinished
        tvTimer.text = formatTime(remainingTimeInMillis)
      }

      override fun onFinish() {
        tvQuestion.text = "Hết thời gian! Đáp án đúng là: $correctAnswer"
        incorrectAnswers++ // Mark question as incorrect
        displayScore()

        // Automatically proceed to the next question
        setupQuiz()
      }
    }.start()
  }

  // Format time for display
  private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    return String.format("%02d:%02d", seconds / 60, seconds % 60)
  }

  // Navigate to the score activity
  private fun navigateToScoreActivity() {
    val quizEndTime = System.currentTimeMillis()
    val totalTimeTaken = quizEndTime - quizStartTime
    val intent = Intent(this, ScoreActivity::class.java)
    intent.putExtra("SCORE", totalScore) // Send total score
    intent.putExtra("DIFFICULTY", difficulty) // Send chosen difficulty
    intent.putExtra("TIME_TAKEN", totalTimeTaken) // Send total time taken
    startActivity(intent)
    finish()
  }
}
