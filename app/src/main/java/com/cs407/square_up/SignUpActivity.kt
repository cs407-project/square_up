package com.cs407.square_up

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button


class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        val sign_up_button = findViewById<Button>(R.id.sign_up_button)
        sign_up_button.setOnClickListener {
            finish()
        }
}
}