package com.cs407.square_up
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("USER_ID", 1)
        setContentView(R.layout.activity_budget)

        val progressBar = findViewById<ProgressBar>(R.id.stats_progressbar)
        val progressText = findViewById<TextView>(R.id.progress_text)

        // Set initial progress
        val progressValue = 75
        progressBar.progress = progressValue
        progressText.text = "$progressValue%"

        // Example: Animate progress
        ObjectAnimator.ofInt(progressBar, "progress", 0, progressValue).apply {
            duration = 1000
            start()
        }
//        val db = AppDatabase.getDatabase(this)
//        val budgetDao = db.budgetDao()
//        val budgetItems = budgetDao.getBudgets(userId)
        val budgetListLayout = findViewById<LinearLayout>(R.id.budgetListLayout)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        loadUserBudgets(userId)
        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, AddBudgetItemActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserBudgets(userID: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val budgets =
                AppDatabase.getDatabase(applicationContext).budgetDao().getBudgets(userID)
            Log.d("BudgetActivity", "Fetched budgets: $budgets")

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                val budgetListLayout = findViewById<LinearLayout>(R.id.budgetListLayout)
                budgetListLayout.visibility = LinearLayout.VISIBLE
                // Clear existing views to avoid duplication
                budgetListLayout.removeAllViews()
                Log.d("BudgetActivity", "budgetListLayout visibility: ${budgetListLayout.visibility}")
                Log.d("BudgetActivity2", "budgetListLayout visibility: ${budgetListLayout.visibility}")
                // Dynamically add each budget to the layout
                budgets.forEach { budget ->
                    val itemView =
                        layoutInflater.inflate(R.layout.budget_item, budgetListLayout, false)

                    val categoryTextView = itemView.findViewById<TextView>(R.id.categoryTextView)
                    val amountTextView = itemView.findViewById<TextView>(R.id.amountTextView)

                    categoryTextView.text = budget.selectedBudget
                    amountTextView.text = "$${budget.currentAmount}"
                    Log.d("BudgetActivity3", "budget: ${budget.selectedBudget}")

                    budgetListLayout.addView(itemView)
                    budgetListLayout.visibility = LinearLayout.VISIBLE
                }
            }
        }
    }
}