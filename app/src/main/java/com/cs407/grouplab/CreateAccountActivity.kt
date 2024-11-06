package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)


        val clickableTextView: TextView = findViewById(R.id.continue_button)
        clickableTextView.setOnClickListener {
            // Action when the text is clicked
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }






    }

}