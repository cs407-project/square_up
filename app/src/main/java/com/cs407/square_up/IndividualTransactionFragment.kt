package com.cs407.square_up


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


class IndividualTransactionFragment : Fragment() {
    private var currentUserId: Int = -1 // Default invalid user ID


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUserId = it.getInt("USER_ID", -1) // Retrieve the user ID passed in arguments
        }
    }
    //    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_individual_transaction, container, false)
        val transactionContainer = rootView.findViewById<LinearLayout>(R.id.transactionContainer)
        // Initialize the database
        val db = AppDatabase.getDatabase(requireContext())
        val transactionDao = db.transactionDao()


        CoroutineScope(Dispatchers.IO).launch {
            // Fetch transactions for the current user from the database
            val transactionData = transactionDao.getIndividualTransactions(currentUserId)
            // Convert transactions to List<Pair<String, String>> for display
            val transactions = transactionData.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd") // Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val description = transaction.transactionDetails
                val id = transaction.transactionID
                val amount = if (transaction.amountOwed < 0) {
                    "-$${"%.2f".format(-transaction.amountOwed)}"
                } else {
                    "$${"%.2f".format(transaction.amountOwed)}"
                }
                "Transaction ID: $id $date\nYou made a transaction for $description of $amount.\n\n"
                //"$date: You made a transaction for $description of $amount. Transaction ID: $id."
            }


            // Add each transaction to the container
            withContext(Dispatchers.Main) {
                for (transaction in transactions) {
                    val transactionView = TextView(requireContext())
                    transactionView.text = "${transaction}"
                    transactionView.textSize = 16f
                    transactionView.setPadding(0, 16, 0, 16) // Space between transactions
                    transactionView.setTextColor(Color.RED)
                    transactionContainer.addView(transactionView)
                }
            }
        }
        return rootView


    }
}
