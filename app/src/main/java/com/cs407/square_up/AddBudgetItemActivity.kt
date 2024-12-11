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
        val addButton2 = findViewById<Button>(R.id.addButton2)



        clearAmountButton.setOnClickListener {
            amountInput.text.clear()
        }
        clearCategoryButton.setOnClickListener {
            categoryInput.text.clear()
        }

        addButton.setOnClickListener {
            val budgetID = 0
            val selectedBudget = categoryInput.text.toString()
            val currentAmount = amountInput.text.toString().toDouble()
            val total = 0.0
            lifecycleScope.launch(Dispatchers.IO) {
                addBudget(budgetID, userId, selectedBudget, currentAmount, total)
            }
//
        }

        populateBudgetTags(userId)
        populateTransactions(userId)

        addButton2.setOnClickListener {
            val budgetCat = findViewById<Spinner>(R.id.addBudg).selectedItem.toString()
            val trans = findViewById<Spinner>(R.id.addTrans).selectedItem.toString().toInt()
            lifecycleScope.launch(Dispatchers.IO) {
                updateTotalAndBudget(trans, budgetCat, userId)

            }
        }


    }
    //    val newBudget = Budget(
//        budgetID = 0, // Leave as 0; Room will auto-generate the ID
//        userID = 123, // Example user ID
//        selectedBudget = categoryInput.text.toString(),
//        currentAmount = amountInput.text.toString().toLong() // Example amount
//    )
//

    private suspend fun updateTotalAndBudget(transactionId: Int, category: String, userId: Int) {

        val db2 = AppDatabase.getDatabase(applicationContext)
        val transDao = db2.transactionDao()
        val db = AppDatabase.getDatabase(applicationContext)
        val budgetDao = db.budgetDao()
        val amountOwed = transDao.getTotal(transactionId)[0]
        val current = budgetDao.getCurrentTotal(userId, category)[0]
        val total = amountOwed + current
        transDao.updateTransactionBudgetTag(userId, category)
        budgetDao.updateTotal(category, total)
    }
    private suspend fun addBudget(budgetID2: Int, userID2: Int, selectedBudget2: String, currentAmount2: Double, total2: Double) {
        val newBudget = Budget(
            budgetID = budgetID2, // Leave as 0; Room will auto-generate the ID
            userID = userID2, // Example user ID
            selectedBudget = selectedBudget2,
            currentAmount = currentAmount2, // Example amount
            total = total2
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

    private fun populateBudgetTags(userId: Int) {
        val budgets = findViewById<Spinner>(R.id.addBudg)

        lifecycleScope.launch(Dispatchers.IO) {
            val db2 = AppDatabase.getDatabase(applicationContext)
            val budgetDao = db2.budgetDao()
            val tags = budgetDao.getBudgets(userId) // Should return List<String>

            withContext(Dispatchers.Main) {
                Log.d("BudgetTags", "Fetched Tags: $tags")
                if (tags.isNotEmpty()) {
                    // Pass the List<String> directly to the adapter
                    val adapter = ArrayAdapter(
                        this@AddBudgetItemActivity,
                        android.R.layout.simple_spinner_item,
                        tags.map { it.toString() } // Explicitly convert to strings
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    budgets.adapter = adapter
                } else {
                    val adapter = ArrayAdapter(
                        this@AddBudgetItemActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("No Budget Tags Available")
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

    private fun populateTransactions(userId: Int) {
        val transactions = findViewById<Spinner>(R.id.addTrans)

        lifecycleScope.launch(Dispatchers.IO) {
            val db2 = AppDatabase.getDatabase(applicationContext)
            val transDao = db2.transactionDao()
            val tags = transDao.getTrans(userId) // Should return List<Int>

            withContext(Dispatchers.Main) {
                Log.d("TransTags", "Fetched Tags: $tags")
                if (tags.isNotEmpty()) {
                    // Pass the List<String> directly to the adapter
                    val adapter = ArrayAdapter(
                        this@AddBudgetItemActivity,
                        android.R.layout.simple_spinner_item,
                        tags.map { it.toInt() } // Explicitly convert to strings
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    transactions.adapter = adapter
                } else {
                    val adapter = ArrayAdapter(
                        this@AddBudgetItemActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("No Transaction Tags Available")
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    transactions.adapter = adapter
                }
                transactions.setSelection(0)
                // Add onItemSelectedListener
                transactions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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