package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class QuizPracticeActivity : AppCompatActivity() {

  private lateinit var tvQuestion: TextView
  private lateinit var btnAnswer1: Button
  private lateinit var btnAnswer2: Button
  private lateinit var btnAnswer3: Button
  private lateinit var btnAnswer4: Button
  private lateinit var ivPlayerImage: ImageView
  private lateinit var btnBack: Button

  private lateinit var players: List<QuizzModel>
  private lateinit var correctAnswer: String
  private var questionIndex = 0
  private var difficultyMix: List<String> = listOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_quiz_practice)

    tvQuestion = findViewById(R.id.tvQuestion)
    btnAnswer1 = findViewById(R.id.btnAnswer1)
    btnAnswer2 = findViewById(R.id.btnAnswer2)
    btnAnswer3 = findViewById(R.id.btnAnswer3)
    btnAnswer4 = findViewById(R.id.btnAnswer4)
    ivPlayerImage = findViewById(R.id.ivPlayerImage)
    btnBack = findViewById(R.id.btnBack)

    difficultyMix = generateRandomDifficultyMix()

    val firebaseRepo = FirebaseRepository()
    firebaseRepo.getPlayersData { playerList ->
      players = playerList
      setupQuiz()
    }

    setupAnswerButtons()

    btnBack.setOnClickListener {
      val intent = Intent(this, DifficultySelectionActivity::class.java)
      startActivity(intent)
      finish()
    }
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

  private fun setupQuiz() {
    if (questionIndex >= difficultyMix.size) {
      questionIndex = 0
    }

    val currentDifficulty = difficultyMix[questionIndex]
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
  }

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
    btnAnswer1.text = answers[0]
    btnAnswer2.text = answers[1]
    btnAnswer3.text = answers[2]
    btnAnswer4.text = answers[3]
  }

  private fun setupAnswerButtons() {
    val clickListener = { view: Button ->
      val selectedAnswer = view.text.toString()
      if (selectedAnswer == correctAnswer) {

        if (btnAnswer1.text == correctAnswer) btnAnswer1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer2.text == correctAnswer) btnAnswer2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer3.text == correctAnswer) btnAnswer3.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer4.text == correctAnswer) btnAnswer4.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        tvQuestion.text = "Đúng rồi!"
      } else {
        tvQuestion.text = "Sai rồi, đáp án đúng là: $correctAnswer"
        view.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))

        // Tô màu xanh cho nút có câu trả lời đúng
        if (btnAnswer1.text == correctAnswer) btnAnswer1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer2.text == correctAnswer) btnAnswer2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer3.text == correctAnswer) btnAnswer3.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        if (btnAnswer4.text == correctAnswer) btnAnswer4.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
      }

      view.postDelayed({
        setupQuiz()
        resetAnswerButtonColors()
      }, 2000)
    }

    btnAnswer1.setOnClickListener { clickListener(btnAnswer1) }
    btnAnswer2.setOnClickListener { clickListener(btnAnswer2) }
    btnAnswer3.setOnClickListener { clickListener(btnAnswer3) }
    btnAnswer4.setOnClickListener { clickListener(btnAnswer4) }
  }
  private fun resetAnswerButtonColors() {
    btnAnswer1.setBackgroundColor(resources.getColor(R.color.bg_main))
    btnAnswer2.setBackgroundColor(resources.getColor(R.color.bg_main))
    btnAnswer3.setBackgroundColor(resources.getColor(R.color.bg_main))
    btnAnswer4.setBackgroundColor(resources.getColor(R.color.bg_main))
  }

}
