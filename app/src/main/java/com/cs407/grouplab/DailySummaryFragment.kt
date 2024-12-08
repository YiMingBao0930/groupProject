package com.cs407.grouplab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.cs407.grouplab.data.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var dateButton: Button
    private lateinit var backButton: Button
    private lateinit var dailySummaryTextView: TextView
    private lateinit var db: AppDatabase
    private lateinit var userNutritionLogDao: UserNutritionLogDao

    private val username = "test_user"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize database
        db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
        userNutritionLogDao = db.userNutritionLogDao()

        // Inflate layout
        return inflater.inflate(R.layout.daily_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        pieChart = view.findViewById(R.id.chart)
        dateButton = view.findViewById(R.id.date_button)
        backButton = view.findViewById(R.id.back_button)
        dailySummaryTextView = view.findViewById(R.id.daily_summary_text_view)

        // Set up initial date
        val todayDate = getCurrentDate()
        dateButton.text = todayDate

        // Initial data fetch
        fetchDailyData(todayDate)

        // Set up click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Date picker button
        dateButton.setOnClickListener {
            showDatePicker()
        }

        // Back button - Navigate to the previous fragment using parentFragmentManager
        backButton.setOnClickListener {
            val fragment = AppHomePageFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, fragment) // Replace with the ID of your container
                .addToBackStack(null) // Add to back stack for reverse navigation
                .commit()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(parentFragmentManager, "datePicker")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = convertMillisecondsToDate(selection)
            dateButton.text = selectedDate
            fetchDailyData(selectedDate)
        }
    }

    private fun fetchDailyData(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("DailySummary", "Fetching data for date: $date")
                val dailyNutrition = userNutritionLogDao.getDailyNutrition(username, date)

                CoroutineScope(Dispatchers.Main).launch {
                    if (dailyNutrition != null) {
                        Log.d("DailySummary", "Data found: $dailyNutrition")
                        updateUI(date, dailyNutrition)
                    } else {
                        Log.d("DailySummary", "No data found for date")
                        dailySummaryTextView.text = "No data available for $date"
                        clearChart()
                    }
                }
            } catch (e: Exception) {
                Log.e("DailySummary", "Error fetching data", e)
                CoroutineScope(Dispatchers.Main).launch {
                    dailySummaryTextView.text = "Error fetching data for $date"
                    clearChart()
                }
            }
        }
    }

    private fun updateUI(date: String, dailyNutrition: DailyNutritionSummary) {
        // Update text summary
        dailySummaryTextView.text = """
            Date: $date
            Username: $username
            Calories: ${dailyNutrition.totalCalories} kcal
            Protein: ${dailyNutrition.totalProtein}g
            Carbs: ${dailyNutrition.totalCarbs}g
            Fat: ${dailyNutrition.totalFat}g
        """.trimIndent()

        // Update pie chart
        updatePieChart(dailyNutrition)
    }

    private fun updatePieChart(dailyNutrition: DailyNutritionSummary) {
        val pieEntries = listOf(
            PieEntry(dailyNutrition.totalProtein.toFloat(), "Protein"),
            PieEntry(dailyNutrition.totalCarbs.toFloat(), "Carbs"),
            PieEntry(dailyNutrition.totalFat.toFloat(), "Fat")
        )

        val dataSet = PieDataSet(pieEntries, "Nutrition Breakdown").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            valueTextSize = 16f
            valueTextColor = resources.getColor(R.color.white, null)
        }

        val pieData = PieData(dataSet)

        pieChart.apply {
            data = pieData
            setEntryLabelTextSize(14f)
            setEntryLabelColor(resources.getColor(R.color.white, null))
            description.isEnabled = false
            isRotationEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = resources.getColor(R.color.black, null)
            invalidate()
        }
    }

    private fun clearChart() {
        pieChart.clear()
        pieChart.invalidate()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun convertMillisecondsToDate(milliseconds: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(milliseconds))
    }
}
