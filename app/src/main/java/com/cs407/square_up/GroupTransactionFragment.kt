package com.cs407.square_up
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class GroupTransactionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_group_transaction, container, false)

        val transactionContainer = rootView.findViewById<LinearLayout>(R.id.transactionContainer)

        // Example data for group transactions
        val transactions = listOf(
            Pair("Group Transaction 10/12", "$100.00"),
            Pair("Group Transaction 10/3", "-$40.00")
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