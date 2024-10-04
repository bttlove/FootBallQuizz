package com.example.footballquizz

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
      } else {
        tvQuestion.text = "Sai rồi, đáp án đúng là: $correctAnswer"
      }

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
}
