package com.cs407.square_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Transaction
import com.cs407.square_up.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Date

class SignInActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)
        GlobalScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@SignInActivity)
            val userDao = db.userDao()
            userDao.getAllUsers() // Query to ensure database is accessed
        }
        username = findViewById(R.id.username) // Make sure these IDs match your layout
        password = findViewById(R.id.password)

        val signUpButton = findViewById<Button>(R.id.sign_up)
        val signInButton = findViewById<Button>(R.id.sign_in)

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        signInButton.setOnClickListener {
            val usernameInput = username.text.toString().trim()
            val passwordInput = password.text.toString().trim()

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {
                    val isValidUser = validateUser(usernameInput, passwordInput)
                    val user= getUser(usernameInput, passwordInput)
                    runOnUiThread {
                        if (isValidUser) {
                            Toast.makeText(this@SignInActivity, "Sign-in successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                            intent.putExtra("USER_ID", user?.userId) // Pass userId in intent
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@SignInActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
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

    private suspend fun validateUser(username: String, password: String): Boolean {
        val hashedPassword = hash(password)
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        return userDao.getUserByCredentials(username, hashedPassword) != null
    }
    private suspend fun getUser(username: String, password: String): User? {
        val hashedPassword = hash(password)
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        return userDao.getUserByCredentials(username, hashedPassword)
    }
}