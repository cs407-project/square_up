package com.cs407.square_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)
        val userId = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val groupId = intent.getIntExtra("GROUP_ID", 1) // Default to 1 if not found
        val imageButton4 = findViewById<ImageButton>(com.cs407.square_up.R.id.imageButton4)
        val db = AppDatabase.getDatabase(this)
        val groupContainer= findViewById<LinearLayout>(com.cs407.square_up.R.id.groupContainer)
        val groupNameText= TextView(this@GroupActivity)
        groupNameText.text = intent.getStringExtra("GROUP_Name" )
        groupNameText.textSize=44f
        groupContainer.addView(groupNameText) // Add text to the container


        lifecycleScope.launch(Dispatchers.IO) {
            val groups = db.groupDao().getGroupsByGroupID(groupId) // Fetch groups for the user
            val currentUser = db.userDao().getUserById(userId)
            val usernames = mutableListOf<String>()
            for (group in groups) {
                if(group.userID != userId){
                    val thisUserID=group.userID
                    val user = db.userDao().getUserById(thisUserID)
                    user?.userName?.let { usernames.add(it) } // Safely add the username if it exists
                }
            }
            
            withContext(Dispatchers.Main) {
                val currentUserText = TextView(this@GroupActivity)
                currentUserText.text = "Your name:${currentUser?.userName}"
                currentUserText.textSize=34f
                groupContainer.addView(currentUserText) // Add text to the container
                for (username  in usernames) {
                    val TextView = TextView(this@GroupActivity)
                    TextView.text =username// Set  userName as  text
                    TextView.textSize=34f
                    groupContainer.addView(TextView) // Add text to the container
                }

            }
        }
        imageButton4.setOnClickListener {
            val intent = Intent(this, AddGroupMember::class.java)
            intent.putExtra("GROUP_ID",groupId) // Pass groupId in intent
            intent.putExtra("USER_ID", userId) // Pass userId in intent
            startActivity(intent)
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }
}