package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//jump to SignInActivity.xml
        val signInButton: ImageButton = findViewById(R.id.signin_button)
        signInButton.setOnClickListener {
            // create Intent to jump to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }


    }
}