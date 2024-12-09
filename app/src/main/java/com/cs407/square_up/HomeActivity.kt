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
import android.widget.TextView
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

        lifecycleScope.launch(Dispatchers.Main) {
            val totalAmount = getTotalAmountOwedByUser(userId)
            updateTotalAmount(totalAmount)
        }

        temp_finance_button.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            intent.putExtra("USER_ID", userId) // Pass userId in intent
            startActivity(intent)
        }

//        menu_button.setOnClickListener {
//            val intent = Intent(this, HistoryActivity::class.java)
//            intent.putExtra("USER_ID", userId) // Pass userId in intent
//            startActivity(intent)
//        }
        menu_button.setOnClickListener { view ->
            val popupMenu = androidx.appcompat.widget.PopupMenu(this, view)
            popupMenu.menuInflater.inflate(com.cs407.square_up.R.menu.menu_popup, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    com.cs407.square_up.R.id.menu_history -> {
                        val intent = Intent(this, HistoryActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        true
                    }
                    com.cs407.square_up.R.id.menu_change_password -> {
                        val intent = Intent(this, ChangePasswordActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        true
                    }
                    com.cs407.square_up.R.id.menu_sign_out -> {
                        Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
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
            Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignInActivity::class.java)
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
        lifecycleScope.launch(Dispatchers.Main) {
            val totalAmount = getTotalAmountOwedByUser(userId)
            updateTotalAmount(totalAmount)
        }
    }

    // Function to calculate total amount owed by the user
    private suspend fun getTotalAmountOwedByUser(userId: Int): Double {
        val transactions = AppDatabase.getDatabase(applicationContext).transactionDao().getTransactionsByUser(userId)
        var totalAmount = 0.0
        var AmountOwedToUser = 0.0
        var AmountOwedByUser = 0.0
        for (transaction in transactions) {
            // Add or subtract based on whether the user has paid

                if (transaction.initialUser == true) {
                    val transactionID =transaction.transactionID
                    val usersInTranscation=AppDatabase.getDatabase(applicationContext).transactionDao().getOwedTransactionsById(transactionID)
                    for(owedTransaction in usersInTranscation){
                                AmountOwedToUser += owedTransaction.amountOwed
                    }
                } else {
                    if (transaction.paid == false) {
                        AmountOwedByUser += transaction.amountOwed
                    }
                }

        }
        totalAmount=AmountOwedByUser-AmountOwedToUser
        return totalAmount
    }

    // Function to update the total amount owed TextView
    private fun updateTotalAmount(amount: Double) {
        val totalAmountValueTextView = findViewById<TextView>(R.id.total_amount_value)
        val totalAmountTextView = findViewById<TextView>(R.id.total_amount)
//        totalAmountValueTextView.text = "$" +amount.toString()
        totalAmountValueTextView.text =String.format("%.2f", amount)
        // Change color based on positive or negative amount
        if (amount > 0) {
            totalAmountValueTextView.setTextColor(Color.RED)
            totalAmountTextView.setText("Total Amount you owe:")

        }
        else if (amount == 0.0) {
            totalAmountValueTextView.setTextColor(Color.GRAY)
        }
        else {
            totalAmountValueTextView.setTextColor(Color.GREEN)
            totalAmountTextView.setText("Total Amount owed to you:")
            totalAmountValueTextView.text =String.format("%.2f", -amount)
        }
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