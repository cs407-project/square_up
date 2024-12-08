package com.cs407.square_up

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GroupActivity : AppCompatActivity() {
    private lateinit var groupContainer: LinearLayout // Declare as class-level property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)

        val userId = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val groupId = intent.getIntExtra("GROUP_ID", 1) // Default to 1 if not foun
        val GROUP_Name = intent.getStringExtra("GROUP_Name")
        val imageButton4 = findViewById<ImageButton>(com.cs407.square_up.R.id.imageButton4)
        groupContainer = findViewById<LinearLayout>(com.cs407.square_up.R.id.groupContainer)
        loadGroupMembers(userId, groupId)

        imageButton4.setOnClickListener {
            val intent = Intent(this, AddGroupMember::class.java)
            intent.putExtra("GROUP_ID", groupId) // Pass groupId in intent
            intent.putExtra("USER_ID", userId) // Pass userId in intent
            intent.putExtra("GROUP_Name", GROUP_Name) // Pass Name in intent
            startActivity(intent)
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
        val profileButton = findViewById<ImageButton>(R.id.profile_button)
        profileButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val groupId = intent.getIntExtra("GROUP_ID", 1) // Default to 1 if not found
        loadGroupMembers(userId, groupId)
    }

    private fun loadGroupMembers(userId: Int, groupId: Int) {
        val groupNameText = TextView(this@GroupActivity).apply {
            setTextColor(Color.RED)
            text = intent.getStringExtra("GROUP_Name")
            textSize = 44f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        groupContainer.addView(groupNameText) // Add text to the container

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            // Fetching groups for the user and then joining with User table
            val memberUsernames = db.groupDao().getGroupMembersByGroupId(groupId)

            val currentUser = db.userDao().getUserById(userId)

            withContext(Dispatchers.Main) {
                groupContainer.removeAllViews() // Clear existing views
                groupContainer.addView(groupNameText) // Re-add the group name

                val currentUserText = TextView(this@GroupActivity).apply {
                    text = "Your name: ${currentUser?.userName}"
                    textSize = 34f
                    setTextColor(Color.rgb(255, 255, 255)) // Set the text color
                    textAlignment = View.TEXT_ALIGNMENT_CENTER // Center the text
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }
                groupContainer.addView(currentUserText) // Add text to the container

                val label = TextView(this@GroupActivity).apply {
                    textSize = 34f
                    text = "Other Group Members:"
                    setTextColor(Color.rgb(134, 36, 196))
                }
                groupContainer.addView(label)

                val otherMembers = memberUsernames.filter { it != currentUser?.userName }

                if (otherMembers.isEmpty()) {
                    val noMembersText = TextView(this@GroupActivity).apply {
                        text = "No other members in this group."
                        textSize = 34f
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setTextColor(Color.rgb(134, 36, 196))
                    }
                    groupContainer.addView(noMembersText)
                } else {
                    for (username in otherMembers) {
                        val memberTextView = TextView(this@GroupActivity).apply {
                            text = username
                            textSize = 34f
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            setTextColor(Color.rgb(134, 36, 196))
                        }
                        groupContainer.addView(memberTextView) // Add text to the container
                    }
                }
            }
        }
    }
}