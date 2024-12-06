package com.cs407.square_up

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirm_password: EditText
    private lateinit var userPasswdKV: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        // Initialize views
        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        confirm_password = findViewById(R.id.confirm_password)
        val sign_up_button = findViewById<Button>(R.id.sign_up_button)
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        // Initialize SharedPreferences
        userPasswdKV = getSharedPreferences(getString(R.string.userPasswdKV), Context.MODE_PRIVATE)

        // Set up the button click listener
        sign_up_button.setOnClickListener {
            val nameInput = name.text.toString().trim()
            val emailInput = email.text.toString().trim()
            val userNameInput = username.text.toString().trim()
            val passwordInput = password.text.toString().trim()

            if (nameInput.isEmpty() || emailInput.isEmpty() || userNameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "One or more of the fields are empty.", Toast.LENGTH_SHORT).show()
            } else {
                // Use a coroutine to handle database operations
                lifecycleScope.launch(Dispatchers.IO) {
                    val isRegistered = getUserPasswd(userNameInput, passwordInput)
                    runOnUiThread {
                        if (isRegistered) {
                            Toast.makeText(this@SignUpActivity, "User registered successfully", Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                finish()
                            }, 1000)
                        } else {
                            Toast.makeText(this@SignUpActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    private suspend fun getUserPasswd(name: String, passwdPlain: String): Boolean {
        val hashedPassword = hash(passwdPlain)
        val storedHashedPassword = userPasswdKV.getString(name, null)

        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()

        return if (storedHashedPassword != null) {
            hashedPassword == storedHashedPassword
        } else {
            // Register a new user if they don't exist
            val newUser = User(userName = name, password = hashedPassword, email = email.text.toString().trim())
            userDao.insert(newUser)
            Log.d("SignUpActivity", "User inserted with userId: ${newUser.userId}")
            userPasswdKV.edit().putString(name, hashedPassword).apply()
            true
        }
    }
}