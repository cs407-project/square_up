package com.cs407.squareup

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class IndividualTransactionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_individual_transaction, container, false)

        val transactionContainer = rootView.findViewById<LinearLayout>(R.id.transactionContainer)

        // Example data for transactions
        val transactions = listOf(
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/1", "-$20.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 9/22", "$30.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00"),
            Pair("Transaction 10/11", "$50.00")
        )

        // Add each transaction to the container
        for (transaction in transactions) {
            val transactionView = TextView(requireContext())
            transactionView.text = "${transaction.first}: ${transaction.second}"
            transactionView.textSize = 16f
            transactionView.setPadding(0, 16, 0, 16) // Space between transactions
            if (transaction.second.startsWith("-")) {
                transactionView.setTextColor(Color.RED)
            }
            else {
                transactionView.setTextColor(Color.parseColor("#4CAF50"))
            }
            transactionContainer.addView(transactionView)
        }

        return rootView

    }
}