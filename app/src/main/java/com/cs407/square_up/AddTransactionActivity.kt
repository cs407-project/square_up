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
import android.widget.TextView
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale


class AddTransactionActivity : AppCompatActivity() {
    companion object {
        const val CAMERA_REQUEST_CODE = 1001
    }


//    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val imageBitmap: Bitmap = result.data?.extras?.get("data") as Bitmap
//            Log.d("CameraActivity", "Image bitmap: $imageBitmap")
//            if (imageBitmap == null) {
//                Log.e("CameraActivity", "Bitmap is null")
//            }
//        }
//    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                Log.d("CameraActivity", "Image bitmap captured successfully.")
                processBitmapWithMLKit(imageBitmap) // Call ML Kit processing
            } else {
                Log.e("CameraActivity", "Bitmap is null")
            }
        } else {
            Log.e("CameraActivity", "Failed to capture image.")
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

//    private fun handleRecognizedText(visionText: Text) {
//        val stringBuilder = StringBuilder()
//        for (block in visionText.textBlocks) {
//            stringBuilder.append(block.text).append("\n")
//        }
//
//        // Log or display the recognized text
//        Log.d("CameraActivity", "Recognized Text: $stringBuilder")
//
//        // For example, display it in a TextView
//        val textView: TextView = findViewById(R.id.enterDescription_label) // Ensure a TextView exists in your layout
//        textView.text = stringBuilder.toString()
//    }
//
//    private fun extractTotalFromText(text: String): String {
//        // You can define a pattern to extract the total amount
//        val regex = Regex("\\b\\d+(?:\\.\\d{1,2})?\\b") // Basic pattern for extracting numbers
//        val match = regex.find(text)
//        return match?.value ?: "Not found"
//    }

    private fun handleRecognizedText(visionText: Text) {
        val stringBuilder = StringBuilder()
        var totalAmount = 0.0 // Variable to accumulate the total amount

//        val textView: TextView = findViewById(R.id.enterDescription_label)
//        textView.text = stringBuilder.toString()

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
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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