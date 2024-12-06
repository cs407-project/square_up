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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class AddTransactionActivity : AppCompatActivity() {
    companion object {
        const val CAMERA_REQUEST_CODE = 1001
    }


    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    // Create an InputImage object from the image URI
                    val inputImage = InputImage.fromFilePath(this, imageUri)

                    // Initialize ML Kit's Text Recognizer
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                    // Process the image to recognize text
                    recognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            // Handle the successful result here
                            val recognizedText = visionText.text
                            // Process the text to find the total amount
                            val totalAmount = extractTotalFromText(recognizedText)
                            Toast.makeText(this, "Total Amount: $totalAmount", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // Handle any errors here
                            Toast.makeText(this, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

    private fun extractTotalFromText(text: String): String {
        // You can define a pattern to extract the total amount
        val regex = Regex("\\b\\d+(?:\\.\\d{1,2})?\\b") // Basic pattern for extracting numbers
        val match = regex.find(text)
        return match?.value ?: "Not found"
    }




    private val selectedUsers = mutableListOf<String>() // List to store selected users

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTransaction)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Need to add a query "SELECT groupName from GROUPS WHERE userID = :userID"
        val selectGroupSpinner = findViewById<Spinner>(R.id.selectGroup)


        val selectPersonButton = findViewById<Button>(R.id.selectPerson)
        selectPersonButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val userDao = db.userDao()
                val currentUserID =intent.getIntExtra("USER_ID", 1)
                val users = userDao.getAllOtherUsers(currentUserID) // Replace 1 with actual user ID

                withContext(Dispatchers.Main) {
                    showMultiSelectDialog(users)
                }
            }
        }

        val addTransactionButton = findViewById<Button>(R.id.addTransaction3)
        addTransactionButton.setOnClickListener {
            val description = findViewById<EditText>(R.id.enterDescription).text.toString()
            val amount = findViewById<EditText>(R.id.enterAmount).text.toString()
            val splitPercentageText = findViewById<EditText>(R.id.editTextNumberDecimal).text.toString()
            val selectGroup = findViewById<Spinner>(R.id.selectGroup)

            if (description.isEmpty() || amount.isEmpty() || splitPercentageText.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val splitPercentage = splitPercentageText.toDoubleOrNull()
            if (splitPercentage == null || splitPercentage <= 0 || splitPercentage > 100) {
                Toast.makeText(this, "Invalid split percentage", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate that users have been selected
            if (selectedUsers.isEmpty()) {
                Toast.makeText(this, "Please select at least one person.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                userWhoPaidID = 1, // Replace with actual user ID
                transactionAmount = amount.toDouble(),
                transactionDetails = description,
                transactionDate = Date(),
                splitPercentage = splitPercentage,
                paid = false,
                budgetTags = selectedUsers // Using selected users as tags for simplicity
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val transactionDao = db.transactionDao()
                transactionDao.insertTransaction(transaction)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddTransactionActivity, "Transaction added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val customSplitButton = findViewById<Button>(R.id.customSplit)
        customSplitButton.setOnClickListener {
            // Custom split logic
        }

        val equalSplitButton = findViewById<Button>(R.id.equalSplit)
        equalSplitButton.setOnClickListener {
            // Equal split logic
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

    //TODO Add functionality to validate transaction for multiple users from one input
    private suspend fun validateTransaction(transaction: Transaction) {
        val db = AppDatabase.getDatabase(this)
        val transactionDao = db.transactionDao()
        transactionDao.insertTransaction(transaction)
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

}