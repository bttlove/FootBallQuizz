package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView

class DifficultySelectionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

        // Lấy instance của FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Lấy người dùng hiện tại
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Lấy email của người dùng
            val email = currentUser.email

            if (email != null) {
                // Tách phần tên trước dấu @
                val username = email.split("@")[0]

                // Hiển thị tên người dùng trong phần chào mừng
                val greetingTextView: TextView = findViewById(R.id.greeting)
                greetingTextView.text = "Hello, $username!"
            }
        }

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
