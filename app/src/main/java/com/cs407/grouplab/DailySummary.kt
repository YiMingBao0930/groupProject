package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class DailySummary : AppCompatActivity() {

    private val Intake = floatArrayOf(98.8f, 123.8f, 161.6f,)
    private val nutritionList = arrayOf("Protein", "Fat", "Carbohydrates")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.daily_summary)
        setupPieChart()

        val lastPageButton: ImageButton = findViewById(R.id.back_button)
        lastPageButton.setOnClickListener {
            // create Intent to jump to SignInActivity
            val intent = Intent(this, AppHomePage::class.java)
            startActivity(intent)
        }
    }

    private fun setupPieChart() {
        // Populating a list of PieEntries
        val pieEntries = ArrayList<PieEntry>()
        for (i in Intake.indices) {
            pieEntries.add(PieEntry(Intake[i], nutritionList [i]))
        }

        // Creating the data set and setting colors
        val dataSet = PieDataSet(pieEntries, "Your energy intake today")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        // Creating PieData and setting it to the chart
        val data = PieData(dataSet)
        val chart = findViewById<PieChart>(R.id.chart)
        chart.data = data
        chart.animateY(2000)
        chart.invalidate() // Refresh chart with new data
    }
}