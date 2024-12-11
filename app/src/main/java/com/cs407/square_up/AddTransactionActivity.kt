package com.cs407.square_up

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    companion object {
        const val CAMERA_REQUEST_CODE = 1001
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                processBitmapWithMLKit(imageBitmap) // Call ML Kit processing
            } else {
            }
        } else {
        }
    }

    private fun processBitmapWithMLKit(bitmap: Bitmap) {
        // Convert the Bitmap to InputImage
        val inputImage = InputImage.fromBitmap(bitmap, 0) // Replace 0 with correct rotation if needed

        // Initialize ML Kit's TextRecognizer
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Process the image
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                handleRecognizedText(visionText)
            }
            .addOnFailureListener { e ->
                Log.e("CameraActivity", "Text recognition failed: ${e.message}")
            }
    }

    private fun handleRecognizedText(visionText: Text) {
        val stringBuilder = StringBuilder()
        var totalAmount = 0.0 // Variable to accumulate the total amount

        // Process each text block and separate description and amounts
        for (block in visionText.textBlocks) {
            val blockText = block.text.trim()

            // Check if the block is a number (we will treat as amount)
            val regex = Regex("\\b\\d+(?:\\.\\d{1,2})?\\b") // Match numbers (including decimals)
            val matches = regex.findAll(blockText)

            // If there are numbers, we add them up
            for (match in matches) {
                val amount = match.value.toDoubleOrNull()
                if (amount != null) {
                    totalAmount += amount
                }
            }

            // If no numbers are found, we consider the text as part of the description
            if (matches.count() == 0) {
                stringBuilder.append(blockText).append("\n")
            }
        }

        // Populate the EditText for description
        val descriptionEditText: EditText = findViewById(R.id.enterDescription)
        descriptionEditText.setText(stringBuilder.toString().trim())

        // Populate the EditText for amount (sum of numbers)
        val amountEditText: EditText = findViewById(R.id.enterAmount)
        amountEditText.setText(totalAmount.toString())

        // Provide a message to the user
        val message = "The fields have been populated. Please review and edit if necessary."
        val autofillMessage = findViewById<TextView>(R.id.autofill_message)
        autofillMessage.setText(message)
    }

    // Function to extract numbers from the text
    private fun extractNumbersFromText(text: String): List<Double> {
        val regex = Regex("\\b\\d+(?:\\.\\d{1,2})?\\b") // Basic pattern for extracting numbers
        return regex.findAll(text).map { it.value.toDouble() }.toList()
    }

    private val selectedUsers = mutableListOf<String>() // List to store selected users

    override fun onCreate(savedInstanceState: Bundle?) {
        val db = AppDatabase.getDatabase(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_transaction)
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTransaction)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup for group spinner
        val selectGroupSpinner = findViewById<Spinner>(R.id.selectGroup)
        val currentUserID = intent.getIntExtra("USER_ID", 1)
        setupGroupSpinner(db, currentUserID, selectGroupSpinner)

        populateBudgetTags(currentUserID)

        val selectPersonButton = findViewById<Button>(R.id.selectPerson)
        selectPersonButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()
                val users = userDao.getAllOtherUsers(currentUserID)
                withContext(Dispatchers.Main) {
                    showMultiSelectDialog(users)
                }
            }
        }

//        val addTransactionButton = findViewById<Button>(R.id.addTransaction3)
//        addTransactionButton.setOnClickListener {
//            addTransaction(db, currentUserID)
//        }

        val customSplitButton = findViewById<Button>(R.id.customSplit)
        customSplitButton.setOnClickListener {
            showCustomSplitDialog(db, currentUserID)
        }

        val equalSplitButton = findViewById<Button>(R.id.equalSplit)
        equalSplitButton.setOnClickListener {
            performEqualSplit(db, currentUserID)
        }

        val useCameraButton = findViewById<Button>(R.id.useCamera)
        useCameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            } else {
                // Permission granted, launch the camera using cameraLauncher
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(cameraIntent)
            }
        }
    }

    private fun showCustomSplitDialog(db: AppDatabase, currentUserID: Int) {
        if(selectedUsers.isEmpty()) {
            Toast.makeText(this, "No users selected for custom split.", Toast.LENGTH_SHORT).show()
            return
        }

        val layoutInflater = LayoutInflater.from(this)
        val customView = layoutInflater.inflate(R.layout.custom_split_dialog, null)
        val userPercentages = mutableListOf<Pair<String, EditText>>()

        lifecycleScope.launch(Dispatchers.IO) {
            val userDao = db.userDao()
            val allUsers = mutableListOf<String>().apply {
                // Add the logged-in user first
                val currentUser = userDao.getUserById(currentUserID)?.userName
                if (currentUser != null) {
                    add(currentUser)
                }
                // Then add the rest of the group members
                addAll(selectedUsers.filter { it != currentUser })
            }

            withContext(Dispatchers.Main) {
                allUsers.forEach { userName ->
                    val userLayout = layoutInflater.inflate(R.layout.user_percentage_item, null, false)
                    userLayout.findViewById<TextView>(R.id.userNameTextView)?.text = userName
                    val percentageEditText = userLayout.findViewById<EditText>(R.id.percentageEditText)
                    customView.findViewById<LinearLayout>(R.id.usersLayout)?.addView(userLayout)
                    if (percentageEditText != null) {
                        userPercentages.add(Pair(userName, percentageEditText))
                    }
                }

                if(userPercentages.isEmpty()) {
                    Toast.makeText(this@AddTransactionActivity, "Error setting up user percentage inputs.", Toast.LENGTH_SHORT).show()
                    return@withContext
                }

                AlertDialog.Builder(this@AddTransactionActivity)
                    .setTitle("Custom Split")
                    .setView(customView)
                    .setPositiveButton("Apply") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            handleCustomSplit(db, currentUserID, userPercentages)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    private suspend fun handleCustomSplit(db: AppDatabase, currentUserID: Int, userPercentages: List<Pair<String, EditText>>) {
        val transactionDao = db.transactionDao()
        val userDao = db.userDao()
        val amountText = findViewById<EditText>(R.id.enterAmount).text.toString()
        val amount = amountText.toDouble()
        val description = findViewById<EditText>(R.id.enterDescription).text.toString()
        val userBudgetTag = findViewById<Spinner>(R.id.enterBudgetTag).selectedItem.toString()
        val transactionDate = Date()
        val nextId = (transactionDao.getMaxTransactionId() ?: 0) + 1

        var totalPercentage = BigDecimal.ZERO
        val userSplits = mutableListOf<Pair<Int, BigDecimal>>()

        userPercentages.forEach { (userName, editText) ->
            val percentage = editText.text.toString().toDoubleOrNull() ?: 0.0
            val userId = userDao.getUserByName(userName)?.userId ?: return@forEach
            if (percentage > 0 && percentage <= 1) {
                totalPercentage = totalPercentage.add(BigDecimal.valueOf(percentage))
                userSplits.add(Pair(userId, BigDecimal.valueOf(percentage)))
            }
        }

        if (totalPercentage.compareTo(BigDecimal.ONE) == 0) {
            userSplits.forEach { (userId, percentage) ->
                val amountOwed = BigDecimal.valueOf(amount).multiply(percentage).setScale(5, RoundingMode.HALF_UP)
                var check = ""
                if (currentUserID == userId) {
                    check = userBudgetTag
                } else {
                    check = ""
                }
                transactionDao.insertTransaction(
                    Transaction(
                        transactionID = nextId,
                        userWhoPaidID = userId,
                        transactionAmount = amount,
                        transactionDetails = description,
                        transactionDate = transactionDate,
                        splitPercentage = percentage.toDouble(),
                        paid = userId == currentUserID,
                        budgetTag = check,
                        amountOwed = amountOwed.setScale(2, RoundingMode.HALF_UP).toDouble(),
                        initialUser = userId == currentUserID
                    )
                )
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddTransactionActivity, "Custom split transaction added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddTransactionActivity, "Total percentage must equal 100%", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupGroupSpinner(db: AppDatabase, currentUserID: Int, spinner: Spinner) {
        lifecycleScope.launch(Dispatchers.IO) {
            val groupsDao = db.groupDao()
            val groupNames = groupsDao.getGroupNamesByUser(currentUserID)
            withContext(Dispatchers.Main) {
                if (groupNames.isNotEmpty()) {
                    val groupNamesWithPlaceholder = mutableListOf("Select a group") + groupNames
                    val adapter = ArrayAdapter(this@AddTransactionActivity, R.layout.spinner_item, groupNamesWithPlaceholder)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.setSelection(0)
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val selectedGroup = parent?.getItemAtPosition(position).toString()
                            if (selectedGroup != "Select a group") {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val groupId = groupsDao.getGroupIdByName(currentUserID, selectedGroup)
                                    if (groupId != null) {
                                        val memberNames = groupsDao.getGroupMembersByGroupId(groupId)
                                        withContext(Dispatchers.Main) {
                                            selectedUsers.clear()
                                            selectedUsers.addAll(memberNames)
                                            Toast.makeText(this@AddTransactionActivity, "Selected: $selectedGroup, Users: $selectedUsers", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                selectedUsers.clear()
                                Toast.makeText(this@AddTransactionActivity, "Please select a valid group", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            Toast.makeText(this@AddTransactionActivity, "No selection made", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("SpinnerSetup", "No groups found for user ID: $currentUserID")
                    Toast.makeText(this@AddTransactionActivity, "No groups found for this user.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun addTransaction(db: AppDatabase, currentUserID: Int) {
        val description = findViewById<EditText>(R.id.enterDescription).text.toString()
        val amountText = findViewById<EditText>(R.id.enterAmount).text.toString()
        val userBudgetTag = findViewById<Spinner>(R.id.enterBudgetTag).toString()

        if (description.isEmpty() || amountText.isEmpty() || selectedUsers.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields and select users", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()
        lifecycleScope.launch(Dispatchers.IO) {
            val transactionDao = db.transactionDao()
            val userDao = db.userDao()
            val transactionDate = Date()
            val nextId = (transactionDao.getMaxTransactionId() ?: 0) + 1

            // Here would be where you would implement the split logic based on the button clicked
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddTransactionActivity, "Transaction added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun performEqualSplit(db: AppDatabase, currentUserID: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val transactionDao = db.transactionDao()
            val userDao = db.userDao()
            val amountText = findViewById<EditText>(R.id.enterAmount).text.toString()
            val amount = amountText.toDouble()
            val description = findViewById<EditText>(R.id.enterDescription).text.toString()
            val userBudgetTag = findViewById<Spinner>(R.id.enterBudgetTag).selectedItem.toString()
            val transactionDate = Date()
            val nextId = (transactionDao.getMaxTransactionId() ?: 0) + 1

            val allUsers = mutableListOf<String>().apply {
                val currentUser = userDao.getUserById(currentUserID)?.userName
                if (currentUser != null && !selectedUsers.contains(currentUser)) {
                    add(currentUser)
                }
                addAll(selectedUsers)
            }

            // Calculate split percentage with higher precision
            val splitPercentage = BigDecimal.ONE.divide(BigDecimal(allUsers.size), 20, RoundingMode.HALF_UP)

            allUsers.forEach { userName ->
                val userId = userDao.getUserByName(userName)?.userId ?: return@forEach
                // Use setScale to ensure only 2 decimal places for the percentage in the database
                val percentageForUser = splitPercentage.setScale(5, RoundingMode.HALF_UP)
                val amountOwed = BigDecimal(amount).multiply(percentageForUser).setScale(5, RoundingMode.HALF_UP)
                var check = ""
                if (currentUserID == userId) {
                    check = userBudgetTag
                } else {
                    check = ""
                }
                transactionDao.insertTransaction(
                    Transaction(
                        transactionID = nextId,
                        userWhoPaidID = userId,
                        transactionAmount = amount,
                        transactionDetails = description,
                        transactionDate = transactionDate,
                        splitPercentage = percentageForUser.toDouble(),
                        paid = userId == currentUserID,
                        budgetTag = check,
                        amountOwed = amountOwed.setScale(2, RoundingMode.HALF_UP).toDouble(),
                        initialUser = userId == currentUserID
                    )
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddTransactionActivity, "Equal split transaction added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showMultiSelectDialog(users: List<String>) {
        // Prepare a list of available users
        val availableUsers = users.toTypedArray()

        // Create a temporary list to track selections during the dialog session
        val tempSelectedUsers = selectedUsers.toMutableList()
        val selectedItems = BooleanArray(availableUsers.size) { index ->
            selectedUsers.contains(availableUsers[index])
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Users")
        builder.setMultiChoiceItems(availableUsers, selectedItems) { _, which, isChecked ->
            if (isChecked) {
                tempSelectedUsers.add(availableUsers[which]) // Add user to temporary list if checked
            } else {
                tempSelectedUsers.remove(availableUsers[which]) // Remove user from temporary list if unchecked
            }
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            // Update the main selectedUsers list regardless of the number of selections
            selectedUsers.clear()
            selectedUsers.addAll(tempSelectedUsers)

            if (selectedUsers.isEmpty()) {
                // Show a toast if no users are selected
                Toast.makeText(this, "No users selected. Please choose at least one.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Selected Users: $selectedUsers", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Discard any changes made during this session
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, launch camera
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } else {
            // Permission denied, show a message or handle appropriately
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun populateBudgetTags(userId: Int) {
//        val budgets = findViewById<Spinner>(R.id.enterBudgetTag)
//
//        lifecycleScope.launch(Dispatchers.IO) {
//
//            val db2 = AppDatabase.getDatabase(applicationContext)
//            val budget = db2.budgetDao()
//            val tags = budget.getBudgets(userId)
//
//            withContext(Dispatchers.Main) {
//
//                val adapter = ArrayAdapter(
//                    this@AddTransactionActivity,
//                    android.R.layout.simple_spinner_item,
//                    tags
//                )
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                budgets.adapter = adapter
//            }
//        }
//
//    }

    private fun populateBudgetTags(userId: Int) {
        val budgets = findViewById<Spinner>(R.id.enterBudgetTag)

        lifecycleScope.launch(Dispatchers.IO) {
            val db2 = AppDatabase.getDatabase(applicationContext)
            val budgetDao = db2.budgetDao()
            val tags = budgetDao.getBudgets(userId) // Should return List<String>

            withContext(Dispatchers.Main) {
                Log.d("BudgetTags", "Fetched Tags: $tags")
                if (tags.isNotEmpty()) {
                    // Pass the List<String> directly to the adapter
                    val adapter = ArrayAdapter(
                        this@AddTransactionActivity,
                        android.R.layout.simple_spinner_item,
                        tags.map { it.toString() } // Explicitly convert to strings
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    budgets.adapter = adapter
                } else {
                    val adapter = ArrayAdapter(
                        this@AddTransactionActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("")
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    budgets.adapter = adapter
                }
                budgets.setSelection(0)
                // Add onItemSelectedListener
                budgets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = parent?.getItemAtPosition(position).toString()
                        Log.d("SpinnerSelection", "Selected item: $selectedItem")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        Log.d("SpinnerSelection", "Nothing selected")
                    }
                }
            }
        }
    }

}