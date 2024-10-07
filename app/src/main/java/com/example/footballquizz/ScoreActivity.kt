package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ScoreActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var btnRestart: Button // Nút để chơi lại

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        tvScore = findViewById(R.id.tvScore)
        btnRestart = findViewById(R.id.btnRestart) // Khởi tạo nút

        // Nhận điểm từ intent
        val score = intent.getIntExtra("SCORE", 0)
        tvScore.text = "Tổng điểm của bạn: $score"

        // Thiết lập sự kiện click cho nút "Chơi lại"
        btnRestart.setOnClickListener {
            // Chuyển về QuizActivity để bắt đầu lại
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
            finish() // Đóng ScoreActivity
        }
    }
}
