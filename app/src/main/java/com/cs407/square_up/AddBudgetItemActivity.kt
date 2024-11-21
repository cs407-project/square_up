package com.cs407.square_up

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

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
            val newBudget = Budget(
                budgetID = 0, // Leave as 0; Room will auto-generate the ID
                userID = 123, // Example user ID
                selectedBudget = categoryInput.text.toString(),
                currentAmount = amountInput.text.toString().toLong() // Example amount
            )
//            GlobalScope.launch {
//                MyApp.database.budgetDao().insertUser(newBudget)
//            }
        }


    }
}