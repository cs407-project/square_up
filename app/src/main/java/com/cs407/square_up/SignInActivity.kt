package com.cs407.square_up

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        val signUpButton = findViewById<Button>(R.id.sign_up)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        val signInButton = findViewById<Button>(R.id.sign_in)
        signInButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
<<<<<<< Updated upstream
=======

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
>>>>>>> Stashed changes
}

