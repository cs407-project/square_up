package com.cs407.square_up

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordPaymentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UnpaidTransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_payment)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val recordPaymentButton = findViewById<Button>(R.id.recordPayment)
        recordPaymentButton.setOnClickListener {
            Toast.makeText(this, "Click on Record to Pay", Toast.LENGTH_LONG).show()
            loadUnpaidTransactions()
        }

        recyclerView = findViewById(R.id.unpaidTransactionsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadUnpaidTransactions() {
        // Assuming currentUserID is passed from another activity or stored in the app
        val currentUserID = intent.getIntExtra("USER_ID", -1)
        if (currentUserID == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val transactionsDao = db.transactionDao()
            val unpaidTransactions = transactionsDao.getAllUnpaidTransactionsForUser(currentUserID)

            withContext(Dispatchers.Main) {
                adapter = UnpaidTransactionsAdapter(unpaidTransactions) { transaction ->
                    showTransactionDetails(transaction)
                }
                recyclerView.adapter = adapter
            }
        }
    }

    private fun showTransactionDetails(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.transaction_details_dialog, null)
        dialogView.findViewById<TextView>(R.id.transactionDescription).text = transaction.transactionDetails
        dialogView.findViewById<TextView>(R.id.transactionAmount).text = "${transaction.amountOwed}"
        dialogView.findViewById<TextView>(R.id.transactionDate).text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(transaction.transactionDate)

        val recordPaymentButton = dialogView.findViewById<Button>(R.id.recordPaymentButton)
        recordPaymentButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(applicationContext)
                val transactionDao = db.transactionDao()
                transaction.paid = true
                Log.d("RecordPaymentActivity", "Transaction paid status before update: ${transaction.paid}")
                transactionDao.update(transaction)
                val updatedTransaction = transactionDao.getTransactionById(transaction.transactionID)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecordPaymentActivity, "Payment Recorded", Toast.LENGTH_SHORT).show()
                    adapter.removeItem(transaction)
                }
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
    }
}

class UnpaidTransactionsAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<UnpaidTransactionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.transactionDescription)
        val amount: TextView = view.findViewById(R.id.transactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.description.text = "You Owe: $${transaction.amountOwed} for ${transaction.transactionDetails}"
        holder.amount.text = "Transaction ID: ${transaction.transactionID}"
        holder.itemView.setOnClickListener { onItemClick(transaction) }
    }

    override fun getItemCount() = transactions.size

    fun removeItem(transaction: Transaction) {
        val index = transactions.indexOf(transaction)
        if (index != -1) {
            transactions = transactions.filter { it != transaction }
            notifyItemRemoved(index)
        }
    }
}