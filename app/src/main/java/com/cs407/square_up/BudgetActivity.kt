package com.cs407.square_up
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class BudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, AddBudgetItemActivity::class.java)
            startActivity(intent)
        }
    }
}