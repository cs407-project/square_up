package com.cs407.square_up
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RecordPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_payment)
        val recordPayment= findViewById<Button>(R.id.recordPayment)
        recordPayment.setOnClickListener {

        }
    }
}