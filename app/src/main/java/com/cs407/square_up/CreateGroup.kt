package com.cs407.square_up

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button


class CreateGroup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)
        val CreateGroupButton = findViewById<Button>(R.id.CreateGroupButton)
        CreateGroupButton.setOnClickListener {
            finish()
        }
    }
}