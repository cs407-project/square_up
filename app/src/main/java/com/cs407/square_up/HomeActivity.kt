package com.cs407.square_up
import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cs407.square_up.R.layout.home)

        val group1Button = findViewById<Button>(com.cs407.square_up.R.id.group_1_button)
        val group2Button = findViewById<Button>(com.cs407.square_up.R.id.group_2_button)
        val group3Button = findViewById<Button>(com.cs407.square_up.R.id.group_3_button)

        group1Button.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }

        group2Button.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }

        group3Button.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }

    }
}