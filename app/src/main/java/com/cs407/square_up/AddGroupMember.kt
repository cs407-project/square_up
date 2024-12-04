package com.cs407.square_up

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date


class AddGroupMember : AppCompatActivity() {
    private val selectedMembers = mutableListOf<String>() // List to store selected members

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_group_member)
        val db = AppDatabase.getDatabase(this)
        val groupDao = db.groupDao()
        val userDao = db.userDao()
        val currentUserID = intent.getIntExtra("USER_ID", 1) // Default to 1 if not found
        val groupId = intent.getIntExtra("GROUP_ID", 1) // Default to 1 if not found
        val sharedID = intent.getIntExtra("SHARED_ID", 1) // Default to 1 if not found
        val groupNameNullable = intent.getStringExtra("GROUP_Name")
        if (groupNameNullable == null) {
            Toast.makeText(this, "Error: groupName is null", Toast.LENGTH_SHORT).show()
            finish()
            return // Exit the function to avoid further processing
        }
        val groupName = groupNameNullable // Now groupName is guaranteed to be non-null
        // Fetch users and display them in a multi-select dialog
        val  selectMembersButton= findViewById<Button>(R.id.selectMembers)
        selectMembersButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val userDao = db.userDao()
                    val users = userDao.getAllOtherUsers(currentUserID)

                    withContext(Dispatchers.Main) {
                        showMultiSelectDialog(users)
                    }
                }
            }
        }
        val addMemberButton = findViewById<Button>(R.id.addMemberButton)
        addMemberButton.setOnClickListener {
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
                for (userID in memberIDs) {
                    val group = Group(
                        userID = userID,
                        groupName = groupName,
                        dateCreated = Date(),
                        sharedGroupID=sharedID
                    )
                    groupDao.insertGroup(group)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddGroupMember, "Added successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            finish()
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