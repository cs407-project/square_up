package com.cs407.square_up
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cs407.square_up.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class GroupTransactionFragment : Fragment() {
    private var currentUserId: Int = -1 // Default invalid user ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUserId = it.getInt("USER_ID", -1) // Retrieve the user ID passed in arguments
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_group_transaction, container, false)

        val transactionContainer = rootView.findViewById<LinearLayout>(R.id.transactionContainer)

        // Initialize the database
        val db = AppDatabase.getDatabase(requireContext())
        val transactionDao = db.transactionDao()

        // Example data for group transactions
        val transactions = listOf(
            Pair("Group Transaction 10/12", "$100.00"),
            Pair("Group Transaction 10/3", "-$40.00")
        )
//        CoroutineScope(Dispatchers.IO).launch {
            // Fetch transactions for the current user from the database
//            val transactionData = transactionDao.getTransactionsByUser(currentUserId)
            // Convert transactions to List<Pair<String, String>> for display
//            val transactions = transactionData.map { transaction ->
//                val dateFormatter = SimpleDateFormat("MM/dd") // Format date as "MM/dd"
//                val date = dateFormatter.format(transaction.transactionDate)
//                val amount = if (transaction.transactionAmount < 0) {
//                    "-$${"%.2f".format(-transaction.transactionAmount)}"
//                } else {
//                    "$${"%.2f".format(transaction.transactionAmount)}"
//                }
//                Pair("Transaction $date", amount)
//            }
//
//            withContext(Dispatchers.Main) {
                // Add each transaction to the container
                for (transaction in transactions) {
                    val transactionView = TextView(requireContext())
                    transactionView.text = "${transaction.first}: ${transaction.second}"
                    transactionView.textSize = 16f
                    transactionView.setPadding(0, 16, 0, 16) // Space between transactions
                    if (transaction.second.startsWith("-")) {
                        transactionView.setTextColor(Color.RED)
                    } else {
                        transactionView.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    transactionContainer.addView(transactionView)
//                }
//            }
        }
        return rootView
    }
}