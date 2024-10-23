package com.example.footballquizz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeAdminActivity : AppCompatActivity() {

    private lateinit var adminNameTextView: TextView
    private lateinit var goToAdminButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)

        // Initialize views
        adminNameTextView = findViewById(R.id.adminNameTextView)
        goToAdminButton = findViewById(R.id.goToAdminButton)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Lấy thông tin người dùng hiện tại
        val currentUser = auth.currentUser
        currentUser?.let {
            // Lấy email của người dùng hiện tại
            val email = currentUser.email

            // Truy vấn Firestore để lấy username từ email
            if (email != null) {
                db.collection("user").document(email).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Lấy username từ Firestore và hiển thị lời chào
                            val username = document.getString("username")
                            adminNameTextView.text = "Hello, ${ username?: "Admin"}"
                        } else {
                            adminNameTextView.text = "Hello"
                        }
                    }
                    .addOnFailureListener { e ->
                        adminNameTextView.text = "Error fetching username"
                    }
            }
        }

        // Chuyển đến AdminActivity khi nhấn vào nút
        goToAdminButton.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }
}
