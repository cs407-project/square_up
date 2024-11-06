package com.cs407.square_up

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView


class AddGroupMember : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_group_member)
        val addMemberButton = findViewById<Button>(R.id.addMemberButton)
        addMemberButton.setOnClickListener {
            finish()
        }
    }
}