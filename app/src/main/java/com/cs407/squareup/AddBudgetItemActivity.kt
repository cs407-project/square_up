package com.cs407.squareup
import android.os.Bundle
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

        clearAmountButton.setOnClickListener {
            amountInput.text.clear()
        }
        clearCategoryButton.setOnClickListener{
            categoryInput.text.clear()
        }
    }
}