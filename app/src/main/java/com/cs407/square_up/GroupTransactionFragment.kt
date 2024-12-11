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


        CoroutineScope(Dispatchers.IO).launch {
            // Fetch group transactions for the current user from the database
            //val transactionData = transactionDao.getGroupTransactions(currentUserId)


            val groupTransactions = transactionDao.getGroupTransactions(currentUserId)


            // Fetch transactions where the user has been paid back
            val paidBackTransactions = transactionDao.getTransactionsPaidBackToUser(currentUserId)




            // all of these are set to negative
            val groupTransactionList = groupTransactions.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd") // Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val amount = "-$${"%.2f".format(-transaction.amountOwed)}"
//                val amount = if (transaction.amountOwed < 0) {
//                    "-$${"%.2f".format(-transaction.amountOwed)}"
//                } else {
//                    "$${"%.2f".format(transaction.amountOwed)}"
//                }
                //Pair("Group Transaction $date", amount)
                Triple(transaction.transactionDate, "Group Transaction $date", amount)
            }


            // all of these are set to positive (someone paying them back)
            val paidBackTransactionList = paidBackTransactions.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd") // Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val amount = "$${"%.2f".format(transaction.amountOwed)}"
//                val amount = if (transaction.amountOwed < 0) {
//                    "-$${"%.2f".format(-transaction.amountOwed)}"
//                } else {
//                    "$${"%.2f".format(transaction.amountOwed)}"
//                }
                //Pair("Group Transaction $date", amount)
                Triple(transaction.transactionDate, "Group Transaction $date", amount)
            }


            val combinedTransactions = groupTransactionList + paidBackTransactionList
            val sortedTransactions = combinedTransactions.sortedBy { it.first }
            val finalTransactionList = sortedTransactions.map {
                Pair(it.second, it.third)
            }


            // Add each transaction to the container
            withContext(Dispatchers.Main) {
                for (transaction in finalTransactionList) {
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
                }
            }
        }


        return rootView
    }
}
