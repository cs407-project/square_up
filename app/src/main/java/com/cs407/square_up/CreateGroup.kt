package com.cs407.square_up

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

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

                val currentMaxId = groupDao.getMaxGroupId() ?: 0
                val newGroupId = currentMaxId + 1

                for (userID in memberIDs) {
                    val existingGroup = groupDao.getGroupByUserNameAndGroupName(userID, groupName)
                    if (existingGroup == null) {
                        val group = Group(
                            groupID = newGroupId,
                            userID = userID,
                            groupName = groupName,
                            dateCreated = Date()
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
