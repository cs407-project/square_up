package com.cs407.square_up


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class CreateGroup : AppCompatActivity() {

    private lateinit var db: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        // Initialize the database
        db = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java, "note_database"
        ).build()

        val name = findViewById<EditText>(R.id.enterGroupName)
        val createGroupButton = findViewById<Button>(R.id.CreateGroupButton)

        createGroupButton.setOnClickListener {
            val groupName = name.text.toString().trim()

            if (groupName.isNotEmpty()) {
                // Assume userID is available (e.g., from the logged-in user)
//                val userId = getCurrentUserId() // Replace this with your method for retrieving the current user ID

                // Insert the group into the database
                lifecycleScope.launch(Dispatchers.IO) {
                    val placeholderUserId = -1 // Temporary placeholder for userID

                    val group = Group(userID = placeholderUserId, groupName = groupName, dateCreated = Date())
                    db.groupDao().insertGroup(group)
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
