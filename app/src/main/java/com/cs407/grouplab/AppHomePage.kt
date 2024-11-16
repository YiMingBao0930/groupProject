package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView


class AppHomePage : AppCompatActivity() {
    private val Intake = floatArrayOf(98.8f, 123.8f, 161.6f,)
    private val nutritionList = arrayOf("Protein", "Fat", "Carbohydrates")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_home_page)

        setupPieChart()



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_food -> {
                    val intent = Intent(this, Food::class.java)
                    startActivity(intent)
                    true
                }
                // Handle other menu items
                R.id.nav_calendar -> {

                    true
                }
                R.id.nav_walk -> {
                    // Handle walk selection
                    true
                }
                R.id.nav_home -> {
                    // Handle home selection
                    true
                }
                R.id.nav_stats -> {
                    val intent = Intent(this, Stats::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
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

