package com.cs407.square_up
//import android.R
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton;
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeActivity : AppCompatActivity() {
    private lateinit var groupContainer: LinearLayout // Declare as class-level property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cs407.square_up.R.layout.home)

//        val group1Button = findViewById<Button>(com.cs407.square_up.R.id.group_1_button)
//        val group2Button = findViewById<Button>(com.cs407.square_up.R.id.group_2_button)
//        val group3Button = findViewById<Button>(com.cs407.square_up.R.id.group_3_button)
//        var groupContainer= findViewById<LinearLayout>(com.cs407.square_up.R.id.group_container)
        val temp_finance_button = findViewById<ImageButton>(com.cs407.square_up.R.id.temp_finance_button)
        val menu_button = findViewById<ImageButton>(com.cs407.square_up.R.id.menu_button)
        val addExpense = findViewById<ImageButton>(com.cs407.square_up.R.id.add_expense_button)
        val squareUpButton = findViewById<ImageView>(com.cs407.square_up.R.id.temp_square_up_button)
        val profileButton = findViewById<ImageButton>(com.cs407.square_up.R.id.profile_button)
        val addGroupButton = findViewById<Button>(com.cs407.square_up.R.id.add_group_button)

        val userId = intent.getIntExtra("USER_ID", -1) // Default to -1 if not found
        groupContainer = findViewById(com.cs407.square_up.R.id.group_container) // Initialize the class-level groupContainer here
        loadGroups(userId)

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
    override fun onResume() {
        super.onResume()
        val userId = intent.getIntExtra("USER_ID", -1) // Default to -1 if not found
        loadGroups(userId)
    }

    private fun loadGroups(userId: Int) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val groups = db.groupDao().getGroupsByUser(userId) // Fetch groups for the user
            withContext(Dispatchers.Main) {
                groupContainer.removeAllViews() // Clear existing views
                for (group in groups) {
                    val button = Button(this@HomeActivity)
                    button.text = group.groupName
                    button.setTextColor(Color.rgb(255, 255, 255) )
                    button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#354C61")) // Set background tint
                    button.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL // Center button horizontally
                    } // Set layout parameters to wrap content
                    button.setOnClickListener {
                        val intent = Intent(this@HomeActivity, GroupActivity::class.java)
                        intent.putExtra("GROUP_ID", group.groupID)
                        intent.putExtra("USER_ID", userId)
                        intent.putExtra("GROUP_Name", group.groupName)
                        startActivity(intent)
                    }
                    groupContainer.addView(button)
                }
            }
        }
    }

}