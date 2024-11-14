package com.cs407.grouplab

import android.content.Intent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class NutritionPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutrition_page)

        val signInButton: ImageButton = findViewById(R.id.jumptoscanpage)
        signInButton.setOnClickListener {
            // create Intent to jump to SignInActivity
            val intent = Intent(this, ScanPage::class.java)
            startActivity(intent)
        }
    }
}