package com.cs407.square_up
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
            finish()
        }
        val currentUserID =intent.getIntExtra("USER_ID", 1)
        // Load the default fragment (Individual Transactions) on activity start
        loadFragment(IndividualTransactionFragment().apply {
            arguments = Bundle().apply {
                putInt("USER_ID", currentUserID)
            }
        })

        // Set up click listeners for the bottom navigation buttons
        findViewById<TextView>(R.id.individualTransactions).setOnClickListener {
            loadFragment(IndividualTransactionFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", currentUserID)
                }
            })
        }

        findViewById<TextView>(R.id.groupTransactions).setOnClickListener {
            loadFragment(GroupTransactionFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", currentUserID)
                }
            })
        }
    }

    // Method to load a fragment into the fragment container
    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }

//    // Method to load a fragment into the fragment container and pass userId
//    private fun loadFragment(fragment: Fragment, userId: Int) {
//        val bundle = Bundle()
//        bundle.putInt("USER_ID", userId) // Attach userId to the fragment
//        fragment.arguments = bundle
//
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragmentContainer, fragment)
//        transaction.commit()
//    }
}