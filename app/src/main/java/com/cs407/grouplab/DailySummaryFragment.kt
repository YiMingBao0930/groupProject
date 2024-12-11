package com.cs407.grouplab

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.data.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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
    private lateinit var dayTitle: TextView
    private lateinit var nutritionDetails: TextView
    private lateinit var db: AppDatabase
    private var username: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use the singleton pattern to get database instance
        db = AppDatabase.getDatabase(requireContext())

        // Fetch the current logged-in username from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("logged_in_username", null)

        return inflater.inflate(R.layout.daily_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        pieChart = view.findViewById(R.id.chart)
        dateButton = view.findViewById(R.id.date_button)
        backButton = view.findViewById(R.id.back_button)
        dayTitle = view.findViewById(R.id.day_title)
        nutritionDetails = view.findViewById(R.id.nutrition_details)

        // Check if username is null
        if (username.isNullOrEmpty()) {
            nutritionDetails.text = "Error: No logged-in user found. Please log in again."
            return
        }

        // Set today's date and fetch data
        val todayDate = getCurrentDate()
        dateButton.text = todayDate
        fetchDailyData(todayDate)
        fetchHistoricalData()

        // Set up click listeners
        dateButton.setOnClickListener { showDatePicker() }
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
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
        // Ensure username is not null
        if (username.isNullOrEmpty()) {
            nutritionDetails.text = "Error: No logged-in user found."
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val nutrition = db.userNutritionLogDao().getDailyNutrition(username!!, date)
            CoroutineScope(Dispatchers.Main).launch {
                if (nutrition != null) {
                    dayTitle.text = date
                    nutritionDetails.text = """
                        Calories: ${nutrition.totalCalories} kcal
                        Protein: ${nutrition.totalProtein}g
                        Carbs: ${nutrition.totalCarbs}g
                        Fat: ${nutrition.totalFat}g
                    """.trimIndent()
                    setupPieChart(requireView(), nutrition.totalProtein.toFloat(),
                        nutrition.totalFat.toFloat(), nutrition.totalCarbs.toFloat())
                } else {
                    dayTitle.text = "No Data"
                    nutritionDetails.text = "No nutrition data available for $date."
                    pieChart.clear()
                }
            }
        }
    }

    private fun setupPieChart(view: View, protein: Float, fat: Float, carbs: Float) {
        val pieEntries = ArrayList<PieEntry>().apply {
            if (protein > 0) add(PieEntry(protein, "Protein"))
            if (fat > 0) add(PieEntry(fat, "Fat"))
            if (carbs > 0) add(PieEntry(carbs, "Carbs"))
        }

        val dataSet = PieDataSet(pieEntries, "")
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.protein_color),
            ContextCompat.getColor(requireContext(), R.color.fat_color),
            ContextCompat.getColor(requireContext(), R.color.carb_color)
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 0f

        val data = PieData(dataSet)
        val chart = view.findViewById<PieChart>(R.id.chart)
        chart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.project_background))
        chart.setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.project_background))
        chart.holeRadius = 40f
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setDrawEntryLabels(false)
        chart.data = data
        chart.animateY(2000)
        chart.invalidate()
    }

    private fun fetchHistoricalData() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val historicalData = mutableListOf<DailyNutritionSummary>()

        CoroutineScope(Dispatchers.IO).launch {
            for (i in 1..7) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val date = dateFormat.format(calendar.time)
                val nutrition = db.userNutritionLogDao().getDailyNutrition(username!!, date)
                nutrition?.let { historicalData.add(it) }
            }

            CoroutineScope(Dispatchers.Main).launch {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.history_recycler_view)
                recyclerView?.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = NutritionHistoryAdapter(historicalData)
                }
            }
        }
    }

    private fun getCurrentDate() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private fun convertMillisecondsToDate(milliseconds: Long) = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(milliseconds))

    // Inner class for the RecyclerView adapter
    inner class NutritionHistoryAdapter(private val nutritionList: List<DailyNutritionSummary>) :
        RecyclerView.Adapter<NutritionHistoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dateText: TextView = view.findViewById(R.id.date_text)
            val caloriesText: TextView = view.findViewById(R.id.calories_text)
            val macrosText: TextView = view.findViewById(R.id.macros_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val nutrition = nutritionList[position]

            // Create formatted date string
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -(position + 1))
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val dateString = dateFormat.format(calendar.time)

            holder.dateText.text = dateString
            holder.caloriesText.text = "${nutrition.totalCalories} kcal"
            holder.macrosText.text =
                "P: ${nutrition.totalProtein}g  F: ${nutrition.totalFat}g  C: ${nutrition.totalCarbs}g"
        }
        override fun getItemCount() = nutritionList.size
    }
}