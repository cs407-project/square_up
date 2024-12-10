package com.cs407.square_up
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cs407.square_up.data.AppDatabase
import com.cs407.square_up.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class RecordPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_payment)
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        val youPaidInput = findViewById<EditText>(R.id.youPaidInput)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        val addNotesInput = findViewById<EditText>(R.id.addNotesInput)
        val userID = intent.getIntExtra("userID",1)
        val db = AppDatabase.getDatabase(applicationContext)
        val transactionDao = db.transactionDao()

        val recordPayment= findViewById<Button>(R.id.recordPayment)
        recordPayment.setOnClickListener {
            val paymentAmount = youPaidInput.text.toString().trim()
            val paymentDate = dateInput.text.toString().trim()
            val paymentNotes = addNotesInput.text.toString().trim()
            val amount = paymentAmount.toDouble()
            val date = SimpleDateFormat("dd-MM-yyyy").parse(paymentDate)

            if (paymentAmount.isEmpty() || paymentDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch (Dispatchers.IO) {
                transactionDao.insertTransaction(
                    Transaction(
                        transactionID =  (transactionDao.getMaxTransactionId() ?: 0) + 1,
                        userWhoPaidID = userID,
                        transactionAmount = amount,
                        transactionDetails = paymentNotes,
                        splitPercentage = 1.0,
                        paid = true,
                        amountOwed = amount,
                        initialUser = true,
                        transactionDate = date,
                        budgetTag = ""
                    )
                )

            }


        }
    }
}