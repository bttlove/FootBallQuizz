package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
class DifficultySelectionActivity  : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

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