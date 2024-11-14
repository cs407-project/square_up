package com.cs407.square_up
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.change_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.changePassword)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val notifications= findViewById<CheckBox>(R.id.notifications)
        notifications.setOnClickListener {

        }
        val logout= findViewById<Button>(R.id.logout)
        logout.setOnClickListener {
            finish()
        }
    }
}