package com.cs407.square_up


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
            val membersUsername = name.text.toString().trim()

            if (groupName.isNotEmpty()) {
                // Assume userID is available (e.g., from the logged-in user)
//                val userId = getCurrentUserId() // Replace this with your method for retrieving the current user ID

                // Insert the group into the database
                lifecycleScope.launch(Dispatchers.IO) {
                    val placeholderUserId = -1 // Temporary placeholder for userID
                    val myUserID =userDao.getUserByName(membersUsername)?.userId


                    val group = myUserID?.let { it1 ->
                        Group(
                            userID = it1, groupName = groupName, dateCreated = Date())

                    }
                    if (group != null) {
                        groupDao.insertGroup(group)
                        Log.d("CreateGroup", "Group created with userID: ${group.groupID} and name: $groupName")
                    }



                }

                finish() // Close the activity
            } else {
                // Handle empty group name (e.g., show a Toast or error message)
            }
        }
    }

    // Mock method to get the current user ID; replace with actual logic
    private fun getCurrentUserId(): Int {
        return 1 // Replace with logic to retrieve the actual current user ID
    }
}
