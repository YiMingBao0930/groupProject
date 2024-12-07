package com.cs407.grouplab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryAdapter : RecyclerView.Adapter<DailySummaryAdapter.DayViewHolder>() {
    private var nutritionLogs: List<DailyNutritionSummary> = emptyList()
    private var dates: List<Date> = emptyList()

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTitle: TextView = view.findViewById(R.id.day_title)
        val caloriesText: TextView = view.findViewById(R.id.calories_text)
        val proteinText: TextView = view.findViewById(R.id.protein_text)
        val carbText: TextView = view.findViewById(R.id.carb_text)
        val fatText: TextView = view.findViewById(R.id.fat_text)
        val pieChart: PieChart = view.findViewById(R.id.pie_chart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_summary_item, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val nutrition = nutritionLogs[position]
        val date = dates[position]
        
        // Format date
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        holder.dayTitle.text = dateFormat.format(date)
        
        // Update nutrition info
        holder.caloriesText.text = "Calories: ${nutrition.totalCalories}"
        holder.proteinText.text = "Protein: ${nutrition.totalProtein}g"
        holder.carbText.text = "Carbs: ${nutrition.totalCarbs}g"
        holder.fatText.text = "Fat: ${nutrition.totalFat}g"
        
        // Setup pie chart
        setupPieChart(holder.pieChart, nutrition)
    }

    override fun getItemCount() = nutritionLogs.size

    fun updateData(newLogs: List<DailyNutritionSummary>, newDates: List<Date>) {
        nutritionLogs = newLogs
        dates = newDates
        notifyDataSetChanged()
    }

    private fun setupPieChart(chart: PieChart, nutrition: DailyNutritionSummary) {
        val entries = listOf(
            PieEntry(nutrition.totalProtein.toFloat(), "Protein"),
            PieEntry(nutrition.totalFat.toFloat(), "Fat"),
            PieEntry(nutrition.totalCarbs.toFloat(), "Carbs")
        )

        val dataSet = PieDataSet(entries, "Nutrition")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val data = PieData(dataSet)
        chart.data = data
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.centerText = "Macros"
        chart.animateY(1000)
        chart.invalidate()
    }
} 