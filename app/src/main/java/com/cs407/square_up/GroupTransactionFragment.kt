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
        val userDao = db.userDao()


        CoroutineScope(Dispatchers.IO).launch {
            // Fetch group transactions for the current user from the database
            //val transactionData = transactionDao.getGroupTransactions(currentUserId)


            val groupTransactions = transactionDao.getGroupTransactions(currentUserId)
            // get row of person who current user owes
            val groupFilteredTransactions = groupTransactions.filter { transaction ->
                transaction.initialUser == true
            }

            val userGroupTransactions = transactionDao.getUserGroupTransactions(currentUserId)
            // get all rows of users who owe current user
            val userGroupFilteredTransactions = groupTransactions.filter { transaction ->
                transaction.initialUser == false
            }

            // Fetch transactions where the user has been paid back
            val paidBackTransactions = transactionDao.getTransactionsPaidBackToUser(currentUserId)
//            val filteredTransactions = paidBackTransactions.filter { transaction ->
//                transaction.userWhoPaidID != currentUserId
//            }

            val youPaidBack = transactionDao.youPaidBack(currentUserId)


            // all of these are set to negative. user owes ppl
            val groupTransactionList = groupTransactions.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd HH:mm:ss"); // Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val description = transaction.transactionDetails
                val amount = "-$${"%.2f".format(-transaction.amountOwed)}"
                val owed = transaction.userWhoPaidID
                val owedUserName = userDao.getUserById(owed)?.userName
                val id = transaction.transactionID
                val owe = transactionDao.getTransactionAmountByUser(id, currentUserId)
//                val amount = if (transaction.amountOwed < 0) {
//                    "-$${"%.2f".format(-transaction.amountOwed)}"
//                } else {
//                    "$${"%.2f".format(transaction.amountOwed)}"
//                }
                //Pair("Group Transaction $date", amount)
                //Triple(transaction.transactionDate, "Group Transaction $date", amount)
                val string1 = "Transaction ID: $id \nDate: $date\nYou owe $owedUserName for $description of $owe.\n\n"
                Triple(date, string1, amount)
            }

            // set to negative but user paid
            val userGroupTransactionList = userGroupTransactions.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd HH:mm:ss");// Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val amount = "-$${"%.2f".format(-transaction.amountOwed)}"
                val transactionId = transaction.transactionID
                val description = transaction.transactionDetails
                val paidUser = transaction.userWhoPaidID
                val paidUserName = userDao.getUserById(paidUser)?.userName
//                val amount = if (transaction.amountOwed < 0) {
//                    "-$${"%.2f".format(-transaction.amountOwed)}"
//                } else {
//                    "$${"%.2f".format(transaction.amountOwed)}"
//                }
                //Pair("Group Transaction $date", amount)
                //Triple(transaction.transactionDate, "Group Transaction $date", amount)
                val string2 = "Transaction ID: $transactionId \nDate: $date\n $paidUserName owes you $amount for $description.\n\n"
                Triple(date, string2, amount)
            }


            // all of these are set to positive (someone paying them back)
            val paidBackTransactionList = paidBackTransactions.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd HH:mm:ss");// Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val amount = "$${"%.2f".format(transaction.amountOwed)}"
                val transactionId = transaction.transactionID
                val description = transaction.transactionDetails
                val paidUser = transaction.userWhoPaidID
                val paidUserName = userDao.getUserById(paidUser)?.userName
//                val amount = if (transaction.amountOwed < 0) {
//                    "-$${"%.2f".format(-transaction.amountOwed)}"
//                } else {
//                    "$${"%.2f".format(transaction.amountOwed)}"
//                }
                //Pair("Group Transaction $date", amount)
               // Triple(transaction.transactionDate, "Group Transaction $date", amount)
                val string3 = "Transaction ID: $transactionId \nDate: $date \n$paidUserName paid you back $amount for $description\n\n"
                Triple(date, string3, amount)
            }

            val youPaidBackList = youPaidBack.map { transaction ->
                val dateFormatter = SimpleDateFormat("MM/dd HH:mm:ss"); // Format date as "MM/dd"
                val date = dateFormatter.format(transaction.transactionDate)
                val amount = "$${"%.2f".format(-transaction.amountOwed)}"
                val transactionId = transaction.transactionID
                val description = transaction.transactionDetails
                val paidUser = transaction.userWhoPaidID
                val paidUserName = userDao.getUserById(paidUser)?.userName
                val owe = transactionDao.getTransactionAmountByUser(transactionId, currentUserId)
                val string3 = "Transaction ID: ${transactionId}\nDate: ${date}\nYou paid ${paidUserName} back ${owe} for ${description}\n\n"
                Triple(date, string3, amount)
            }



            val combinedTransactions = groupTransactionList + paidBackTransactionList + userGroupTransactionList + youPaidBackList
            val sortedTransactions = combinedTransactions.sortedBy { it.first }
            val finalTransactionList = sortedTransactions.map {
                Pair(it.second, it.third)
            }


            // Add each transaction to the container
            withContext(Dispatchers.Main) {
                for (transaction in finalTransactionList) {
                    val transactionView = TextView(requireContext())
                    transactionView.text = "${transaction.first}"
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
