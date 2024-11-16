package com.cs407.grouplab

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Objects

class Stats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.stats)

        val lastPageButton: ImageButton = findViewById(R.id.back_button)
        lastPageButton.setOnClickListener {
            // create Intent to jump to SignInActivity
            val intent = Intent(this, AppHomePage::class.java)
            startActivity(intent)
        }

        //distance progress bar
        var distanceProgressBar = findViewById<ProgressBar>(R.id.distance_progress_bar)
        //edit it to goals
        distanceProgressBar.max = 10000
        //edit it to current distance record
        val distanceCurrentProgress = 6000
        ObjectAnimator.ofInt(distanceProgressBar, "progress", distanceCurrentProgress)
            .setDuration(2000)
            .start()
        val distanceText = findViewById<TextView>(R.id.distance_text)



        var proteinProgressBar = findViewById<ProgressBar>(R.id.protein_progress_bar)
        proteinProgressBar.max = 10000
        val proteinCurrentProgress = 8000
        ObjectAnimator.ofInt(proteinProgressBar, "progress", proteinCurrentProgress)
            .setDuration(2000)
            .start()
        val proteinText = findViewById<TextView>(R.id.protein_text)



        var caloriesProgressBar = findViewById<ProgressBar>(R.id.calories_progress_bar)
        caloriesProgressBar.max = 10000
        val caloriesCurrentProgress = 7000
        ObjectAnimator.ofInt(caloriesProgressBar, "progress", caloriesCurrentProgress)
            .setDuration(2000)
            .start()
        val caloriesText = findViewById<TextView>(R.id.calories_text)

        var fatProgressBar = findViewById<ProgressBar>(R.id.fat_progress_bar)
        fatProgressBar.max = 10000
        val fatCurrentProgress = 10000
        ObjectAnimator.ofInt(fatProgressBar, "progress", fatCurrentProgress)
            .setDuration(2000)
            .start()
        val fatText = findViewById<TextView>(R.id.fat_text)

    }
}