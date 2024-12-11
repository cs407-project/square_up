package com.cs407.square_up
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUserId = intent.getIntExtra("USER_ID", 1)
        setContentView(R.layout.activity_budget)

        val addButton2 = findViewById<Button>(R.id.addButton2)


        populateBudgetTags(currentUserId)
        populateTransactions(currentUserId)

        addButton2.setOnClickListener {
            val budgetCat = findViewById<Spinner>(R.id.addBudg).selectedItem.toString()
            val trans = findViewById<Spinner>(R.id.addTrans).selectedItem.toString().toInt()
            if (budgetCat==null){
                Toast.makeText(this, "No Budget selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (trans==null){
                Toast.makeText(this, "No Transaction selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                updateTotalAndBudget(trans, budgetCat, currentUserId)

            }
        }

//        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
//        val progressText = findViewById<TextView>(R.id.budgetName)


        // Set initial progress
        //make dao total / budget
//        val progressValue = 60
//        progressBar.progress = progressValue
//        progressText.text = "$progressValue%"

        // Example: Animate progress
//        ObjectAnimator.ofInt(progressBar, "progress", 0, progressValue).apply {
//            duration = 1000
//            start()
//        }
//        val db = AppDatabase.getDatabase(this)
//        val budgetDao = db.budgetDao()
//        val budgetItems = budgetDao.getBudgets(userId)
//        val budgetListLayout = findViewById<LinearLayout>(R.id.budgetListLayout)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

//        loadUserBudgets(userId)
        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, AddBudgetItemActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        loadUserBudgets(currentUserId)
    }

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
                        this@BudgetActivity,
                        R.layout.spinner_item,
                        tags.map { it.toString() } // Explicitly convert to strings
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    budgets.adapter = adapter
                } else {
                    val adapter = ArrayAdapter(
                        this@BudgetActivity,
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
                        this@BudgetActivity,
                        android.R.layout.simple_spinner_item,
                        tags.map { it.toInt() } // Explicitly convert to strings
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    transactions.adapter = adapter
                } else {
                    val adapter = ArrayAdapter(
                        this@BudgetActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("")
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



    private fun loadUserBudgets(userID: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db2 = AppDatabase.getDatabase(applicationContext)
            val budgetDao = db2.budgetDao()
            val budgets = budgetDao.getAllBudgets(userID)
            Log.d("BudgetActivity", "Fetched budgets: $budgets")

            withContext(Dispatchers.Main) {
                val budgetGraph = findViewById<LinearLayout>(R.id.budgetGraphContainer)
                budgetGraph.removeAllViews()

                budgets.forEach { budget ->
                    val itemView = layoutInflater.inflate(R.layout.pie, budgetGraph, false)
                    val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
                    val budgetName = itemView.findViewById<TextView>(R.id.budgetName)
                    val budgetPercentage = itemView.findViewById<TextView>(R.id.budgetPercent)

                    val percentage = if (budget.currentAmount > 0) {
                        (budget.total / budget.currentAmount * 100).toInt()
                    } else {
                        0
                    }

                    val dollar = budget.total
                    val budget1 = budget.currentAmount
                    var final = (budget1 - dollar).toDouble()
                    if (final <= 0.0) {
                        final = 0.0
                    }

                    budgetName.text = budget.selectedBudget
                    budgetPercentage.text = "$${String.format("%.2f", final)} left"
                    progressBar.progress = percentage

                    budgetGraph.addView(itemView)
                }
            }
        }
    }
            // Update the UI on the main thread
//            withContext(Dispatchers.Main) {
//                val budgetListLayout = findViewById<LinearLayout>(R.id.budgetListLayout)
//                budgetListLayout.visibility = LinearLayout.VISIBLE
//                // Clear existing views to avoid duplication
//                budgetListLayout.removeAllViews()
//                Log.d("BudgetActivity", "budgetListLayout visibility: ${budgetListLayout.visibility}")
//                Log.d("BudgetActivity2", "budgetListLayout visibility: ${budgetListLayout.visibility}")
//                // Dynamically add each budget to the layout
//                budgets.forEach { budget ->
//                    val itemView =
//                        layoutInflater.inflate(R.layout.budget_item, budgetListLayout, false)
//
//                    val categoryTextView = itemView.findViewById<TextView>(R.id.categoryTextView)
//                    val amountTextView = itemView.findViewById<TextView>(R.id.amountTextView)
//
//                    categoryTextView.text = budget.selectedBudget
//                    amountTextView.text = "$${budget.currentAmount}"
//                    Log.d("BudgetActivity3", "budget: ${budget.selectedBudget}")
//
//                    budgetListLayout.addView(itemView)
//                    budgetListLayout.visibility = LinearLayout.VISIBLE
//                }
//            }
//        }
//    }
//



//    @SuppressLint("SetTextI18n")
//    private fun loadUserBudgets(userID: Int) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val budgets =
//                AppDatabase.getDatabase(applicationContext).budgetDao().getBudgets(userID)
//            Log.d("BudgetActivity", "Fetched budgets: $budgets")
//
//            // Update the UI on the main thread
//            withContext(Dispatchers.Main) {
//                val budgetListLayout = findViewById<LinearLayout>(R.id.budgetListLayout)
//                budgetListLayout.visibility = LinearLayout.VISIBLE
//                // Clear existing views to avoid duplication
//                budgetListLayout.removeAllViews()
//                Log.d("BudgetActivity", "budgetListLayout visibility: ${budgetListLayout.visibility}")
//                Log.d("BudgetActivity2", "budgetListLayout visibility: ${budgetListLayout.visibility}")
//                // Dynamically add each budget to the layout
//                budgets.forEach { budget ->
//                    val itemView =
//                        layoutInflater.inflate(R.layout.budget_item, budgetListLayout, false)
//
//                    val categoryTextView = itemView.findViewById<TextView>(R.id.categoryTextView)
//                    val amountTextView = itemView.findViewById<TextView>(R.id.amountTextView)
//
//                    categoryTextView.text = budget.selectedBudget
//                    amountTextView.text = "$${budget.currentAmount}"
//                    Log.d("BudgetActivity3", "budget: ${budget.selectedBudget}")
//
//                    budgetListLayout.addView(itemView)
//                    budgetListLayout.visibility = LinearLayout.VISIBLE
//                }
//            }
//        }
//    }
}