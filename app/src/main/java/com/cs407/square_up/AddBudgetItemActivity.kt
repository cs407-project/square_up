package com.cs407.square_up

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Budget
import com.cs407.square_up.data.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBudgetItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget_item)
        val userId = intent.getIntExtra("USER_ID", 1)
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
            val selectedBudget = categoryInput.text.toString().trim()
            val currentAmount = amountInput.text.toString().toDoubleOrNull() ?: 0.0

            if (selectedBudget.isNotEmpty() && currentAmount > 0) {
                lifecycleScope.launch(Dispatchers.IO) {
                    addBudget(userId, selectedBudget, currentAmount)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddBudgetItemActivity, "Budget '$selectedBudget' added!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid category and amount", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun addBudget(userID: Int, selectedBudget: String, currentAmount: Double) {
        val newBudget = Budget(
            budgetID = 0, // Room will auto-generate this
            userID = userID,
            selectedBudget = selectedBudget,
            currentAmount = currentAmount,
            total = 0.0 // Assuming total starts at 0 for a new budget
        )
        val db = AppDatabase.getDatabase(applicationContext)
        val budgetDao = db.budgetDao()
        budgetDao.insertBudget(newBudget)
    }

//    private fun populateBudgetTags(userId: Int) {
//        val budgets = findViewById<Spinner>(R.id.addBudg)
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
//                    this@AddBudgetItemActivity,
//                    android.R.layout.simple_spinner_item,
//                    tags
//                )
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                budgets.adapter = adapter
//            }
//        }
//
//    }





//    private fun populateTransactions(userId: Int) {
//        val transactions = findViewById<Spinner>(R.id.addTrans)
//
//        lifecycleScope.launch(Dispatchers.IO) {
//
//            val db2 = AppDatabase.getDatabase(applicationContext)
//            val budget = db2.transactionDao()
//            val trans2 = budget.getAllUnpaidTransactionsForUser(userId)
//
//            val transactionItems = trans2.map { transaction ->
//                TransactionItem(transactionId = transaction.transactionID, transactionAmount = transaction.transactionAmount)
//            }
//            withContext(Dispatchers.Main) {
//
//                val adapter = ArrayAdapter(
//                    this@AddBudgetItemActivity,
//                    android.R.layout.simple_spinner_item,
//                    transactionItems.map { it.transactionId }
//                )
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                transactions.adapter = adapter
//            }
//        }
//
//    }
}