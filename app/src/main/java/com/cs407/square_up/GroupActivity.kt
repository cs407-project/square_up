package com.cs407.square_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)

        val imageButton4 = findViewById<ImageButton>(com.cs407.square_up.R.id.imageButton4)
        imageButton4.setOnClickListener {
            val intent = Intent(this, AddGroupMember::class.java)
            startActivity(intent)
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}