package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ExerciseMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.exercise_menu)





        val lastPageButton: ImageButton = findViewById(R.id.back_button)
        lastPageButton.setOnClickListener {
            // create Intent to jump to SignInActivity
            val intent = Intent(this, AppHomePage::class.java)
            startActivity(intent)
        }
    }
}