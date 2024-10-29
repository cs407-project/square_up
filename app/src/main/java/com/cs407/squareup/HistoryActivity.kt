package com.cs407.squareup
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            //placeholder
        }

        // Load the default fragment (Individual Transactions) on activity start
        loadFragment(IndividualTransactionFragment())

        // Set up click listeners for the bottom navigation buttons
        findViewById<TextView>(R.id.individualTransactions).setOnClickListener {
            loadFragment(IndividualTransactionFragment())
        }

        findViewById<TextView>(R.id.groupTransactions).setOnClickListener {
            loadFragment(GroupTransactionFragment())
        }
    }

    // Method to load a fragment into the fragment container
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}