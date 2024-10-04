package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomePageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Đăng xuất khi nhấn vào nút Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this@HomePageActivity, LoginActivity::class.java))
            finish() // Đóng HomePageActivity
        }
    }
}
