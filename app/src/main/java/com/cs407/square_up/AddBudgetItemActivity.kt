package com.cs407.square_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Budget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBudgetItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget_item)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val clearAmountButton = findViewById<ImageView>(R.id.clearAmount)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val clearCategoryButton = findViewById<ImageView>(R.id.clearCategory)
        val categoryInput = findViewById<EditText>(R.id.categoryInput)
        val addButton = findViewById<Button>(R.id.addButton)

        clearAmountButton.setOnClickListener {
            amountInput.text.clear()
        }
        clearCategoryButton.setOnClickListener {
            categoryInput.text.clear()
        }

        addButton.setOnClickListener {
            val budgetID = 0
            val userID = 123
            val selectedBudget = categoryInput.text.toString()
            val currentAmount = amountInput.text.toString().toLong()
            lifecycleScope.launch(Dispatchers.IO) {
                addBudget(budgetID, userID, selectedBudget, currentAmount)
            }
//
        }


    }
//    val newBudget = Budget(
//        budgetID = 0, // Leave as 0; Room will auto-generate the ID
//        userID = 123, // Example user ID
//        selectedBudget = categoryInput.text.toString(),
//        currentAmount = amountInput.text.toString().toLong() // Example amount
//    )
//
    private suspend fun addBudget(budgetID2: Int, userID2: Int, selectedBudget2: String, currentAmount2: Long) {
        val newBudget = Budget(
            budgetID = budgetID2, // Leave as 0; Room will auto-generate the ID
            userID = userID2, // Example user ID
            selectedBudget = selectedBudget2,
            currentAmount = currentAmount2 // Example amount
        )
        val db = AppDatabase.getDatabase(this)
        val budgetDao = db.budgetDao()
        budgetDao.insertBudget(newBudget)
    }
}