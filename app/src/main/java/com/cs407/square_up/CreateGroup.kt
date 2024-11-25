package com.cs407.square_up


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Group
//import com.cs407.square_up.data.Group
import com.cs407.square_up.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class CreateGroup : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        // Initialize the database
//        db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "note_database"
//        ).build()
        val db = AppDatabase.getDatabase(this)
        val groupDao = db.groupDao()
        val userDao = db.userDao()

        val name = findViewById<EditText>(R.id.enterGroupName)
        val createGroupButton = findViewById<Button>(R.id.CreateGroupButton)
        val enterUsername = findViewById<EditText>(R.id.enterMemberUsername)



        createGroupButton.setOnClickListener {
            val groupName = name.text.toString().trim()
            // Check if the group name is empty, then closes the app
            if (groupName.isEmpty()) {
                Toast.makeText(this@CreateGroup, "Group name required", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            }
            val membersUsername = enterUsername.text.toString().trim()
            val usernamesArray = membersUsername.split(",")
            if (usernamesArray.isEmpty()) {
                Toast.makeText(this@CreateGroup, "Usernames required", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                for (username in usernamesArray) {
                    // Insert the group into the database
                    lifecycleScope.launch(Dispatchers.IO) {
                        val myUserID = userDao.getUserByName(username)?.userId
                        val group = myUserID?.let { it1 ->
                            Group(
                                userID = it1, groupName = groupName, dateCreated = Date()
                            )

                        }
                        if (group != null) {
                            groupDao.insertGroup(group)
                            runOnUiThread {
                                Toast.makeText(this@CreateGroup, "Group created successfully", Toast.LENGTH_SHORT).show()
                            }
                            Log.d(
                                "CreateGroup",
                                "Group created with groupID: ${group.groupID} , userID: ${group.userID} and name: $groupName"
                            )
                        }else{
                            Log.d("CreateGroup", "Group creation failed")
                            runOnUiThread {
                                Toast.makeText(this@CreateGroup, "Group creation failed for username: $username", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                }
            }
//            Toast.makeText(this@CreateGroup, "Group created successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}
