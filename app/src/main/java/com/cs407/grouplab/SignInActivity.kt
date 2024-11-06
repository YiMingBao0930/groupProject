package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val clickableTextView: TextView = findViewById(R.id.createAccountButton)
        clickableTextView.setOnClickListener {

            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        val clickableTextView2: TextView = findViewById(R.id.sign_in_button)
        clickableTextView2.setOnClickListener {
            val intent = Intent(this, AppHomePage::class.java)
            startActivity(intent)
        }
    }
}
