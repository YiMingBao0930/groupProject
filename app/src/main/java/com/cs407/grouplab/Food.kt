package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class Food : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.food)

        val signInButton: ImageButton = findViewById(R.id.jumptoscanpage)
        signInButton.setOnClickListener {
            // create Intent to jump to SignInActivity
            val intent = Intent(this, ScanPage::class.java)
            startActivity(intent)
        }
    }
}