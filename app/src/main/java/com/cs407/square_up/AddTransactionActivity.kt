package com.cs407.square_up

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.add_transaction)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTransaction)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val customSplit= findViewById<Button>(R.id.customSplit)
        customSplit.setOnClickListener {

        }
        val equalSplit= findViewById<Button>(R.id.equalSplit)
        equalSplit.setOnClickListener {

        }
        val useCamera= findViewById<Button>(R.id.useCamera)
        useCamera.setOnClickListener {

        }
    }
}