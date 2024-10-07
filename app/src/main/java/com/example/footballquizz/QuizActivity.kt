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

class QuizActivity : AppCompatActivity() {

  private lateinit var tvQuestion: TextView
  private lateinit var btnAnswer1: Button
  private lateinit var btnAnswer2: Button
  private lateinit var btnAnswer3: Button
  private lateinit var btnAnswer4: Button
  private lateinit var ivPlayerImage: ImageView
  private var easyQuestionsAsked = 0
  private var mediumQuestionsAsked = 0
  private var hardQuestionsAsked = 0
  private var correctAnswers = 0
  private var incorrectAnswers = 0

  private lateinit var players: List<QuizzModel>
  private lateinit var correctAnswer: String
  private var difficulty: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_quiz)

    // Khởi tạo các view
    tvQuestion = findViewById(R.id.tvQuestion)
    btnAnswer1 = findViewById(R.id.btnAnswer1)
    btnAnswer2 = findViewById(R.id.btnAnswer2)
    btnAnswer3 = findViewById(R.id.btnAnswer3)
    btnAnswer4 = findViewById(R.id.btnAnswer4)
    ivPlayerImage = findViewById(R.id.ivPlayerImage)

    difficulty = intent.getStringExtra("DIFFICULTY")

    // Lấy dữ liệu cầu thủ từ Firebase
    val firebaseRepo = FirebaseRepository()
    firebaseRepo.getPlayersData { playerList ->
      players = playerList
      setupQuiz()
    }

    setupAnswerButtons()
  }

  // Hàm để đặt câu hỏi, sẽ gọi lại mỗi khi người dùng trả lời
  private fun setupQuiz() {
    if (players.isEmpty()) {
      tvQuestion.text = "Không có dữ liệu cầu thủ!"
      return
    }
    // Kiểm tra số lượng câu hỏi đã hỏi cho từng độ khó
    when (difficulty) {
      "easy" -> {
        if (easyQuestionsAsked >= 5) {
          navigateToScoreActivity()
          return
        }
        easyQuestionsAsked++
      }
      "medium" -> {
        if (mediumQuestionsAsked >= 5) {
          navigateToScoreActivity()
          return
        }
        mediumQuestionsAsked++
      }
      "hard" -> {
        if (hardQuestionsAsked >= 5) {
          navigateToScoreActivity()
          return
        }
        hardQuestionsAsked++
      }
    }

    // Chọn ngẫu nhiên một cầu thủ
    val player = players.random()

    // Log thông tin cầu thủ để kiểm tra
    Log.d("QuizActivity", "Cầu thủ: ${player.Name}, Club: ${player.club}, Năm sinh: ${player.yearOfBirth}")

    // Hiển thị ảnh của cầu thủ
    Glide.with(this).load(player.imageUrl).into(ivPlayerImage)

    // Tạo câu hỏi và đáp án dựa trên độ khó
    when (difficulty) {
      "easy" -> {
        tvQuestion.text = "Cầu thủ ${player.Name} này thuộc câu lạc bộ nào?"
        correctAnswer = player.club
        setupAnswerOptions(player.club, player.Name, player.yearOfBirth.toString())
      }
      "medium" -> {
        tvQuestion.text = "Cầu thủ này tên gì?"
        correctAnswer = player.Name
        setupAnswerOptions(player.Name, player.club, player.yearOfBirth.toString())
      }
      "hard" -> {
        tvQuestion.text = "Cầu thủ ${player.Name} này sinh năm bao nhiêu?"
        correctAnswer = player.yearOfBirth.toString()
        setupAnswerOptions(player.yearOfBirth.toString(), player.club, player.Name)
      }
    }
  }

  // Thiết lập các lựa chọn đáp án
  private fun setupAnswerOptions(correct: String, wrong1: String, wrong2: String) {
    val answers = mutableListOf(correct)
    val wrongAnswers = players.filter {
      it.Name != correct &&
        it.club != wrong1 &&
        it.yearOfBirth.toString() != wrong2
    }
      .shuffled()
      .take(3) // Lấy 3 câu trả lời sai

    answers.addAll(wrongAnswers.map {
      when (difficulty) {
        "easy" -> it.club
        "medium" -> it.Name
        "hard" -> it.yearOfBirth.toString()
        else -> ""
      }
    })

    answers.shuffle() // Trộn tất cả các câu trả lời

    // Gán đáp án cho các nút
    btnAnswer1.text = answers[0]
    btnAnswer2.text = answers[1]
    btnAnswer3.text = answers[2]
    btnAnswer4.text = answers[3]
  }

  // Thiết lập sự kiện nhấn vào các nút
  private fun setupAnswerButtons() {
    val clickListener = View.OnClickListener { view ->
      val selectedAnswer = (view as Button).text.toString()
      if (selectedAnswer == correctAnswer) {
        tvQuestion.text = "Đúng rồi!"
        correctAnswers++ // Tăng số câu đúng
      } else {
        tvQuestion.text = "Sai rồi, đáp án đúng là: $correctAnswer"
        incorrectAnswers++ // Tăng số câu sai
      }

      // Hiển thị điểm số
      displayScore()

      // Sau khi trả lời xong, đợi 1 giây rồi nạp câu hỏi mới
      view.postDelayed({
        setupQuiz() // Nạp câu hỏi tiếp theo
      }, 1000)
    }

    // Gán sự kiện cho tất cả các nút
    btnAnswer1.setOnClickListener(clickListener)
    btnAnswer2.setOnClickListener(clickListener)
    btnAnswer3.setOnClickListener(clickListener)
    btnAnswer4.setOnClickListener(clickListener)
  }
  private fun displayScore() {
    val score = calculateScore()
    tvQuestion.append("\nCâu đúng: $correctAnswers\nCâu sai: $incorrectAnswers\nĐiểm: $score")
  }
  private fun calculateScore(): Int {
    var score = 0
    // Tính điểm dựa trên số câu đúng
    score += easyQuestionsAsked * 1 // 1 điểm cho mỗi câu dễ
    score += mediumQuestionsAsked * 5 // 5 điểm cho mỗi câu trung bình
    score += hardQuestionsAsked * 10 // 10 điểm cho mỗi câu khó

    return score
  }
  private fun navigateToScoreActivity() {
    val intent = Intent(this, ScoreActivity::class.java)
    intent.putExtra("SCORE", calculateScore()) // Truyền điểm vào intent
    startActivity(intent)
    finish() // Đóng activity hiện tại
  }
}
