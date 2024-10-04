package com.example.footballquizz
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.view.View
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
    private lateinit var players: List<QuizzModel> // Danh sách cầu thủ
    private lateinit var correctAnswer: String // Đáp án đúng
    private var difficulty: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvQuestion = findViewById(R.id.tvQuestion)
        btnAnswer1 = findViewById(R.id.btnAnswer1)
        btnAnswer2 = findViewById(R.id.btnAnswer2)
        btnAnswer3 = findViewById(R.id.btnAnswer3)
        btnAnswer4 = findViewById(R.id.btnAnswer4)


        // Nhận độ khó từ Intent
        difficulty = intent.getStringExtra("DIFFICULTY")

        // Lấy dữ liệu cầu thủ từ Firebase
        val firebaseRepo = FirebaseRepository()
        firebaseRepo.getPlayersData { playerList ->
            players = playerList
            setupQuiz()
        }

        setupAnswerButtons()
    }

    private fun setupQuiz() {
        // Chọn một cầu thủ ngẫu nhiên
        val player = players.random()
        // Hiển thị hình ảnh cầu thủ
        Glide.with(this).load(player.imageUrl).into(ivPlayerImage)
        when (difficulty) {
            "easy" -> {
                tvQuestion.text = "Cầu thủ này thuộc câu lạc bộ nào?"
                correctAnswer = player.club
                setupAnswerOptions(player.club, player.name, player.yearOfBirth.toString())
            }
            "medium" -> {
                tvQuestion.text = "Cầu thủ này tên gì?"
                correctAnswer = player.name
                setupAnswerOptions(player.name, player.club, player.yearOfBirth.toString())
            }
            "hard" -> {
                tvQuestion.text = "Cầu thủ này sinh năm bao nhiêu?"
                correctAnswer = player.yearOfBirth.toString()
                setupAnswerOptions(player.yearOfBirth.toString(), player.club, player.name)
            }
        }
    }

    private fun setupAnswerOptions(correct: String, wrong1: String, wrong2: String) {
        // Tạo danh sách các đáp án
        val answers =  mutableListOf(correct)

        // Lấy 2 cầu thủ ngẫu nhiên khác để làm đáp án sai
        val wrongAnswers = players.filter { it.name != correct && it.club != wrong1 && it.yearOfBirth.toString() != wrong2 }
            .shuffled()
            .take(2)
        answers.addAll(wrongAnswers.map {
            when (difficulty) {
                "easy" -> it.club // Đáp án sai cho câu hỏi câu lạc bộ
                "medium" -> it.name // Đáp án sai cho câu hỏi tên
                "hard" -> it.yearOfBirth.toString() // Đáp án sai cho câu hỏi năm sinh
                else -> ""
            }
        })
        btnAnswer1.text = answers[0]
        btnAnswer2.text = answers[1]
        btnAnswer3.text = answers[2]
        btnAnswer4.text = "Câu trả lời khác"
    }

    private fun setupAnswerButtons() {
        val clickListener = View.OnClickListener { view ->
            val selectedAnswer = (view as Button).text.toString()
            if (selectedAnswer == correctAnswer) {
                // Đáp án đúng
                tvQuestion.text = "Đáp án đúng!"
            } else {
                // Đáp án sai
                tvQuestion.text = "Đáp án sai, đáp án đúng là: $correctAnswer"
            }
        }

        btnAnswer1.setOnClickListener(clickListener)
        btnAnswer2.setOnClickListener(clickListener)
        btnAnswer3.setOnClickListener(clickListener)
        btnAnswer4.setOnClickListener(clickListener)
    }
}