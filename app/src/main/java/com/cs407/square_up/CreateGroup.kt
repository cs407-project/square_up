//package com.cs407.square_up
//
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import androidx.room.Room
//import com.cs407.square_up.data.AppDatabase
//import com.cs407.square_up.data.Group
////import com.cs407.square_up.data.Group
//import com.cs407.square_up.data.User
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.util.Date
//
//class CreateGroup : AppCompatActivity() {
//
//    private lateinit var db: AppDatabase
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.create_group)
//
//        // Initialize the database
////        db = Room.databaseBuilder(
////            applicationContext,
////            AppDatabase::class.java, "note_database"
////        ).build()
//        val db = AppDatabase.getDatabase(this)
//        val groupDao = db.groupDao()
//        val userDao = db.userDao()
//
//        val name = findViewById<EditText>(R.id.enterGroupName)
//        val createGroupButton = findViewById<Button>(R.id.CreateGroupButton)
//        val enterUsername = findViewById<EditText>(R.id.enterMemberUsername)
//
//
//
//        createGroupButton.setOnClickListener {
//            val groupName = name.text.toString().trim()
//            // Check if the group name is empty, then closes the app
//            if (groupName.isEmpty()) {
//                Toast.makeText(this@CreateGroup, "Group name required", Toast.LENGTH_SHORT).show()
//                finish() // Close the activity
//            }
//            val membersUsername = enterUsername.text.toString().trim()
//            val usernamesArray = membersUsername.split(",")
//            if (usernamesArray.isEmpty()) {
//                Toast.makeText(this@CreateGroup, "Usernames required", Toast.LENGTH_SHORT).show()
//                finish()
//            } else {
//                for (username in usernamesArray) {
//                    // Insert the group into the database
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        val myUserID = userDao.getUserByName(username)?.userId
//                        val group = myUserID?.let { it1 ->
//                            Group(
//                                userID = it1, groupName = groupName, dateCreated = Date()
//                            )
//
//                        }
//                        if (group != null) {
//                            groupDao.insertGroup(group)
//                            runOnUiThread {
//                                Toast.makeText(this@CreateGroup, "Group created successfully", Toast.LENGTH_SHORT).show()
//                            }
//                            Log.d(
//                                "CreateGroup",
//                                "Group created with groupID: ${group.groupID} , userID: ${group.userID} and name: $groupName"
//                            )
//                        }else{
//                            Log.d("CreateGroup", "Group creation failed")
//                            runOnUiThread {
//                                Toast.makeText(this@CreateGroup, "Group creation failed for username: $username", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//
//                    }
//                }
//            }
////            Toast.makeText(this@CreateGroup, "Group created successfully", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//
//    }
//}
package com.cs407.square_up

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CreateGroup : AppCompatActivity() {

    private val selectedMembers = mutableListOf<String>() // List to store selected members

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_group)

        val db = AppDatabase.getDatabase(this)
        val groupDao = db.groupDao()
        val userDao = db.userDao()

        val groupNameEditText = findViewById<EditText>(R.id.enterGroupName)
        val selectMembersButton = findViewById<Button>(R.id.selectMembers)
        val createGroupButton = findViewById<Button>(R.id.CreateGroupButton)
        val currentUserID =intent.getIntExtra("USER_ID", 1)

        // Fetch users and display them in a multi-select dialog
        selectMembersButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val users = userDao.getAllOtherUsers(currentUserID)

                    withContext(Dispatchers.Main) {
                        showMultiSelectDialog(users)
                    }
                }
            }
        }

        createGroupButton.setOnClickListener {
            val groupName = groupNameEditText.text.toString().trim()

            if (groupName.isEmpty()) {
                Toast.makeText(this, "Group name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedMembers.isEmpty()) {
                Toast.makeText(this, "Please select at least one group member", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val memberIDs = mutableListOf<Int>()
                for (username in selectedMembers) {
                    val user = userDao.getUserByName(username)
                    user?.let { memberIDs.add(it.userId) }
                }
                memberIDs.add(currentUserID)

                // Generate a shared groupID (use current time or database's auto-increment)
                val sharedGroupID = (System.currentTimeMillis() / 1000).toInt() // Example: seconds since epoch

                for (userID in memberIDs) {
                    val existingGroup = groupDao.getGroupByUserNameAndSharedID(userID, groupName, sharedGroupID)
                    if (existingGroup == null) {
                        val group = Group(
                            userID = userID,
                            groupName = groupName,
                            dateCreated = Date(),
                            sharedGroupID = sharedGroupID
                        )
                        groupDao.insertGroup(group)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CreateGroup, "User already part of this group!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CreateGroup, "Group created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showMultiSelectDialog(users: List<String>) {
        // Prepare a list of available users
        val availableUsers = users.toTypedArray()

        // Create a temporary list to track selections during the dialog session
        val tempSelectedUsers = selectedMembers.toMutableList()
        val selectedItems = BooleanArray(availableUsers.size) { index ->
            selectedMembers.contains(availableUsers[index])
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Group Members")
        builder.setMultiChoiceItems(availableUsers, selectedItems) { _, which, isChecked ->
            if (isChecked) {
                tempSelectedUsers.add(availableUsers[which]) // Add user to temporary list if checked
            } else {
                tempSelectedUsers.remove(availableUsers[which]) // Remove user from temporary list if unchecked
            }
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            // Update the main selectedUsers list regardless of the number of selections
            selectedMembers.clear()
            selectedMembers.addAll(tempSelectedUsers)

            if (selectedMembers.isEmpty()) {
                // Show a toast if no users are selected
                Toast.makeText(this, "No Group Members selected. Please choose at least one.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Selected Users: $selectedMembers", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Discard any changes made during this session
            dialog.dismiss()
        }
        builder.create().show()
    }

}
