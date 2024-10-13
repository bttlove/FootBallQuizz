package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DifficultySelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

        // Thiết lập thanh điều hướng
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Thiết lập nút chọn độ khó
        findViewById<Button>(R.id.btnEasy).setOnClickListener {
            startQuizActivity("easy")
        }

        findViewById<Button>(R.id.btnMedium).setOnClickListener {
            startQuizActivity("medium")
        }

        findViewById<Button>(R.id.btnHard).setOnClickListener {
            startQuizActivity("hard")
        }
    }

    private fun startQuizActivity(difficulty: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("DIFFICULTY", difficulty)
        startActivity(intent)
    }
}
