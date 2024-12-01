package com.cs407.square_up
//import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cs407.square_up.R.layout.home)

        val group1Button = findViewById<Button>(com.cs407.square_up.R.id.group_1_button)
        val group2Button = findViewById<Button>(com.cs407.square_up.R.id.group_2_button)
        val group3Button = findViewById<Button>(com.cs407.square_up.R.id.group_3_button)
        val temp_finance_button = findViewById<ImageButton>(com.cs407.square_up.R.id.temp_finance_button)
        val menu_button = findViewById<ImageButton>(com.cs407.square_up.R.id.menu_button)
        val addExpense = findViewById<ImageButton>(com.cs407.square_up.R.id.add_expense_button)
        val squareUpButton = findViewById<ImageButton>(com.cs407.square_up.R.id.temp_square_up_button)
        val profileButton = findViewById<ImageButton>(com.cs407.square_up.R.id.profile_button)
        val addGroupButton = findViewById<Button>(com.cs407.square_up.R.id.add_group_button)

        val userId = intent.getIntExtra("USER_ID", -1) // Default to -1 if not found

        group1Button.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        group2Button.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        group3Button.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        temp_finance_button.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        menu_button.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        addExpense.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        squareUpButton.setOnClickListener {
            val intent = Intent(this, RecordPaymentActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }

        addGroupButton.setOnClickListener {
            val intent = Intent(this, CreateGroup::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent

            startActivity(intent)
        }





    }
}