package com.cs407.square_up

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
    private lateinit var groupContainer: LinearLayout // Declare as class-level property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group)
        val userId = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val groupId = intent.getIntExtra("GROUP_ID", 1) // Default to 1 if not found
        val shared_groupID = intent.getIntExtra("SHARED_ID", 1) // Default to 1 if not found
        val GROUP_Name =intent.getStringExtra("GROUP_Name" )
        val imageButton4 = findViewById<ImageButton>(com.cs407.square_up.R.id.imageButton4)
        groupContainer= findViewById<LinearLayout>(com.cs407.square_up.R.id.groupContainer)
        loadGroupMembers(userId, shared_groupID)

        imageButton4.setOnClickListener {
            val intent = Intent(this, AddGroupMember::class.java)
            intent.putExtra("GROUP_ID",groupId) // Pass groupId in intent
            intent.putExtra("USER_ID", userId) // Pass userId in intent
            intent.putExtra("GROUP_Name", GROUP_Name) // Pass Name in intent
            intent.putExtra("SHARED_ID", shared_groupID) // Pass Name in intent
            startActivity(intent)
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val shared_groupID = intent.getIntExtra("SHARED_ID", 1) // Default to 1 if not found
        loadGroupMembers(userId, shared_groupID)
    }
    private fun loadGroupMembers( userId: Int, shared_groupID: Int) {
        val groupNameText = TextView(this@GroupActivity)
        groupNameText.setTextColor(Color.RED)
        groupNameText.text = intent.getStringExtra("GROUP_Name")
        groupNameText.textSize = 44f
        groupNameText.textAlignment = View.TEXT_ALIGNMENT_CENTER // Center the text horizontally
        groupNameText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Make the TextView span the width of the container
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        groupContainer.addView(groupNameText) // Add text to the container
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val groups = db.groupDao().getGroupsBySharedID(shared_groupID) // Fetch groups for the user
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
                groupContainer.removeAllViews() // Clear existing views
                val currentUserText = TextView(this@GroupActivity).apply {
                    text = "Your name: ${currentUser?.userName}"
                    textSize = 34f
                    setTextColor(Color.rgb(17, 17, 232) )// Set the text color
                    textAlignment = View.TEXT_ALIGNMENT_CENTER // Center the text
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }
                groupContainer.addView(currentUserText) // Add text to the container
                val label = TextView(this@GroupActivity)
                label.textSize=34f
                label.text="Other Group Members:"
                groupContainer.addView(label)
                for (username  in usernames) {
                    val TextView = TextView(this@GroupActivity)
                    TextView.text =username// Set  userName as  text
                    TextView.textSize=34f
                    TextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    TextView.setTextColor(Color.rgb(134, 36, 196))
                    groupContainer.addView(TextView) // Add text to the container
                }

            }
        }
    }
}