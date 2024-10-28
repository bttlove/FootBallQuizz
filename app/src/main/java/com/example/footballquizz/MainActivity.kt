package com.example.footballquizz



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {

    private lateinit var btnChangeEmail: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnSendResetEmail: Button
    private lateinit var btnRemoveUser: Button
    private lateinit var changeEmail: Button
    private lateinit var changePassword: Button
    private lateinit var sendEmail: Button
    private lateinit var remove: Button
    private lateinit var signOut: Button

    private lateinit var oldEmail: EditText
    private lateinit var newEmail: EditText
    private lateinit var password: EditText
    private lateinit var newPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Get firebase auth instance
        auth = FirebaseAuth.getInstance()

        // Get current user
        val user = FirebaseAuth.getInstance().currentUser

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                // User auth state is changed - user is null
                // Launch login activity
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_history -> {
                    // Chuyển đến trang DifficultySelectionActivity
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, DifficultySelectionActivity::class.java))
                    true
                }
                R.id.nav_ranking -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    true
                }
                R.id.nav_home_admin -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    true
                }
                R.id.nav_player_management -> {
                    startActivity(Intent(this, PlayerManagementAdmin::class.java))
                    true
                }
                else -> false
            }
        }

        btnChangeEmail = findViewById(R.id.change_email_button)
        btnChangePassword = findViewById(R.id.change_password_button)
        btnSendResetEmail = findViewById(R.id.sending_pass_reset_button)
        btnRemoveUser = findViewById(R.id.remove_user_button)
        changeEmail = findViewById(R.id.changeEmail)
        changePassword = findViewById(R.id.changePass)
        sendEmail = findViewById(R.id.send)
        remove = findViewById(R.id.remove)
        signOut = findViewById(R.id.sign_out)

        oldEmail = findViewById(R.id.old_email)
        newEmail = findViewById(R.id.new_email)
        password = findViewById(R.id.password)
        newPassword = findViewById(R.id.newPassword)

        oldEmail.visibility = View.GONE
        newEmail.visibility = View.GONE
        password.visibility = View.GONE
        newPassword.visibility = View.GONE
        changeEmail.visibility = View.GONE
        changePassword.visibility = View.GONE
        sendEmail.visibility = View.GONE
        remove.visibility = View.GONE

        progressBar = findViewById(R.id.progressBar)

        if (::progressBar.isInitialized) {
            progressBar.visibility = View.GONE
        }

        btnChangeEmail.setOnClickListener {
            oldEmail.visibility = View.GONE
            newEmail.visibility = View.VISIBLE
            password.visibility = View.GONE
            newPassword.visibility = View.GONE
            changeEmail.visibility = View.VISIBLE
            changePassword.visibility = View.GONE
            sendEmail.visibility = View.GONE
            remove.visibility = View.GONE
        }

        changeEmail.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (user != null && newEmail.text.toString().trim().isNotEmpty()) {
                user.updateEmail(newEmail.text.toString().trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show()
                            signOut()
                            progressBar.visibility = View.GONE
                        } else {
                            Toast.makeText(this@MainActivity, "Failed to update email!", Toast.LENGTH_LONG).show()
                            progressBar.visibility = View.GONE
                        }
                    }
            } else if (newEmail.text.toString().trim().isEmpty()) {
                newEmail.error = "Enter email"
                progressBar.visibility = View.GONE
            }
        }

        btnChangePassword.setOnClickListener {
            oldEmail.visibility = View.GONE
            newEmail.visibility = View.GONE
            password.visibility = View.GONE
            newPassword.visibility = View.VISIBLE
            changeEmail.visibility = View.GONE
            changePassword.visibility = View.VISIBLE
            sendEmail.visibility = View.GONE
            remove.visibility = View.GONE
        }

        changePassword.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (user != null && newPassword.text.toString().trim().isNotEmpty()) {
                if (newPassword.text.toString().trim().length < 6) {
                    newPassword.error = "Password too short, enter minimum 6 characters"
                    progressBar.visibility = View.GONE
                } else {
                    user.updatePassword(newPassword.text.toString().trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@MainActivity, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show()
                                signOut()
                                progressBar.visibility = View.GONE
                            } else {
                                Toast.makeText(this@MainActivity, "Failed to update password!", Toast.LENGTH_SHORT).show()
                                progressBar.visibility = View.GONE
                            }
                        }
                }
            } else if (newPassword.text.toString().trim().isEmpty()) {
                newPassword.error = "Enter password"
                progressBar.visibility = View.GONE
            }
        }

        btnSendResetEmail.setOnClickListener {
            oldEmail.visibility = View.VISIBLE
            newEmail.visibility = View.GONE
            password.visibility = View.GONE
            newPassword.visibility = View.GONE
            changeEmail.visibility = View.GONE
            changePassword.visibility = View.GONE
            sendEmail.visibility = View.VISIBLE
            remove.visibility = View.GONE
        }

        sendEmail.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (oldEmail.text.toString().trim().isNotEmpty()) {
                auth.sendPasswordResetEmail(oldEmail.text.toString().trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Reset password email is sent!", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        } else {
                            Toast.makeText(this@MainActivity, "Failed to send reset email!", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }
                    }
            } else {
                oldEmail.error = "Enter email"
                progressBar.visibility = View.GONE
            }
        }

        btnRemoveUser.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (user != null) {
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Your profile is deleted:( Create an account now!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@MainActivity, SignupActivity::class.java))
                            finish()
                            progressBar.visibility = View.GONE
                        } else {
                            Toast.makeText(this@MainActivity, "Failed to delete your account!", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }
                    }
            }
        }

        signOut.setOnClickListener {
            signOut()
        }
    }

    // Sign out method
    private fun signOut() {
        auth.signOut()
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        if (::authListener.isInitialized) {
            auth.removeAuthStateListener(authListener)
        }
    }
}