package com.cs407.square_up

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.cs407.square_up.data.AppDatabase


class AddGroupMember : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_group_member)
        val db = AppDatabase.getDatabase(this)
        val groupDao = db.groupDao()
        val userDao = db.userDao()
        val userId = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val groupId = intent.getIntExtra("GROUP_ID", 1) // Default to 1 if not found

        val addMemberButton = findViewById<Button>(R.id.addMemberButton)
        addMemberButton.setOnClickListener {

            finish()
        }
    }
}