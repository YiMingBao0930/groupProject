package com.cs407.grouplab

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.coroutines.withContext
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
            fetchDailyData(selectedDate)  // Update visuals for the selected date
        }
    }


    private fun fetchDailyData(date: String) {
        if (username.isNullOrEmpty()) {
            nutritionDetails.text = "Error: No logged-in user found."
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val nutrition = db.userNutritionLogDao().getDailyNutrition(username!!, date)
            val userGoal = db.userGoalDao().getUserGoal(username!!)
            withContext(Dispatchers.Main) {
                if (nutrition != null && userGoal != null) {
                    dayTitle.text = date
                    setupPieChart(requireView(), nutrition.totalProtein.toFloat(), nutrition.totalFat.toFloat(), nutrition.totalCarbs.toFloat())
                    setupProgressBars(
                        protein = nutrition.totalProtein,
                        fat = nutrition.totalFat,
                        carbs = nutrition.totalCarbs,
                        fiber = nutrition.totalFiber,
                        sugar = nutrition.totalSugar,
                        sodium = nutrition.totalSodium,
                        vitaminD = nutrition.totalVitaminD,
                        user_goal = userGoal,
                        nutrition = nutrition
                    )
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
            if (carbs > 0) add(PieEntry(carbs, "Carbohydrates"))
        }

        val dataSet = PieDataSet(pieEntries, "Your energy intake today")
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.protein_color),
            ContextCompat.getColor(requireContext(), R.color.fat_color),
            ContextCompat.getColor(requireContext(), R.color.carb_color)
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        dataSet.valueTypeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_semibold)

        val data = PieData(dataSet)
        val chart = view.findViewById<PieChart>(R.id.chart)
        chart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.project_background))
        chart.setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.project_background))
        chart.holeRadius = 40f
        chart.legend.isEnabled = false
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
                    adapter = NutritionHistoryAdapter(historicalData) { selectedDate ->
                        // Fetch and display data for the selected date
                        fetchDailyData(selectedDate)
                    }
                }
            }
        }
    }


    private fun getCurrentDate() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private fun convertMillisecondsToDate(milliseconds: Long) = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(milliseconds))

    private fun setupProgressBars(
        protein: Int,
        fat: Int,
        carbs: Int,
        fiber: Int,
        sugar: Int,
        sodium: Int,
        vitaminD: Int,
        user_goal: UserGoal,
        nutrition:  DailyNutritionSummary
    ) {

        // Calorie Progress
        val caloriesProgressBar = view?.findViewById<ProgressBar>(R.id.calories_progress_bar)
        ObjectAnimator.ofInt(caloriesProgressBar, "progress", protein).setDuration(2000).start()
        caloriesProgressBar?.progress = nutrition.totalCalories
        caloriesProgressBar?.max = user_goal.dailyCalories
        val caloriesActualGoal = view?.findViewById<TextView>(R.id.calories_actual_goal)
        caloriesActualGoal?.text = "${nutrition.totalCalories} kCal / ${user_goal.dailyCalories} kCal"

        // Protein Progress
        val proteinProgressBar = view?.findViewById<ProgressBar>(R.id.protein_progress_bar)
        ObjectAnimator.ofInt(proteinProgressBar, "progress", protein).setDuration(2000).start()
        proteinProgressBar?.progress = protein
        proteinProgressBar?.max = user_goal.proteinGram
        val proteinActualGoal = view?.findViewById<TextView>(R.id.protein_actual_goal)
        proteinActualGoal?.text = "${protein}g / ${user_goal.proteinGram}g"


        // Fat Progress
        val fatProgressBar = view?.findViewById<ProgressBar>(R.id.fat_progress_bar)
        ObjectAnimator.ofInt(fatProgressBar, "progress", fat).setDuration(2000).start()
        fatProgressBar?.progress = fat
        fatProgressBar?.max = user_goal.fatGram
        val fatActualGoal = view?.findViewById<TextView>(R.id.fat_actual_goal)
        fatActualGoal?.text = "${fat}g / ${user_goal.fatGram}g"

        // Carbs Progress
        val carbProgressBar = view?.findViewById<ProgressBar>(R.id.carb_progress_bar)
        ObjectAnimator.ofInt(carbProgressBar, "progress", carbs).setDuration(2000).start()
        carbProgressBar?.progress = carbs
        carbProgressBar?.max = user_goal.carbGram
        val carbActualGoal = view?.findViewById<TextView>(R.id.carb_actual_goal)
        carbActualGoal?.text = "${carbs}g / ${user_goal.carbGram}g"

        // Fiber Progress
        val fiberProgressBar = view?.findViewById<ProgressBar>(R.id.fiber_progress_bar)
        ObjectAnimator.ofInt(fiberProgressBar, "progress", fiber).setDuration(2000).start()
        fiberProgressBar?.progress = fiber
        fiberProgressBar?.max = 25
        val fiberActualGoal = view?.findViewById<TextView>(R.id.fiber_actual_goal)
        fiberActualGoal?.text = "${fiber}g / 25g"

        // Sugar Progress
        val sugarProgressBar = view?.findViewById<ProgressBar>(R.id.sugar_progress_bar)
        ObjectAnimator.ofInt(sugarProgressBar, "progress", sugar).setDuration(2000).start()
        sugarProgressBar?.progress = sugar
        sugarProgressBar?.max = 50
        val sugarActualGoal = view?.findViewById<TextView>(R.id.sugar_actual_goal)
        sugarActualGoal?.text = "${sugar}g / 50g"

        // Sodium Progress
        val sodiumProgressBar = view?.findViewById<ProgressBar>(R.id.sodium_progress_bar)
        ObjectAnimator.ofInt(sodiumProgressBar, "progress", sodium).setDuration(2000).start()
        sodiumProgressBar?.progress = sodium
        sodiumProgressBar?.max = 2300
        val sodiumActualGoal = view?.findViewById<TextView>(R.id.sodium_actual_goal)
        sodiumActualGoal?.text = "${sodium}mg / 2300mg"

        // Vitamin D Progress
        val vitaminDProgressBar = view?.findViewById<ProgressBar>(R.id.vitamin_d_progress_bar)
        ObjectAnimator.ofInt(vitaminDProgressBar, "progress", vitaminD).setDuration(2000).start()
        vitaminDProgressBar?.progress = vitaminD
        vitaminDProgressBar?.max = 15
        val vitaminDActualGoal = view?.findViewById<TextView>(R.id.vitamin_d_actual_goal)
        vitaminDActualGoal?.text = "${vitaminD}mg / 15mg"
    }


    // Inner class for the RecyclerView adapter
    inner class NutritionHistoryAdapter(
        private val nutritionList: List<DailyNutritionSummary>,
        private val clickListener: (String) -> Unit
    ) : RecyclerView.Adapter<NutritionHistoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dateText: TextView = view.findViewById(R.id.date_text)
            val caloriesText: TextView = view.findViewById(R.id.calories_text)
            val proteinNum: TextView = view.findViewById(R.id.protein_num)
            val carbsNum: TextView = view.findViewById(R.id.carbs_num)
            val fatNum: TextView = view.findViewById(R.id.fat_num)
            val macrosContainer: LinearLayout = view.findViewById(R.id.macros_container)
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.format(calendar.time)

            val displayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val dateString = displayFormat.format(calendar.time)

            // Bind data to the views
            holder.dateText.text = dateString
            holder.caloriesText.text = "${nutrition.totalCalories} kcal"

            // Update macro values
            holder.proteinNum.text = "${nutrition.totalProtein}g"
            holder.carbsNum.text = "${nutrition.totalCarbs}g"
            holder.fatNum.text = "${nutrition.totalFat}g"

            // Add click listener
            holder.itemView.setOnClickListener {
                clickListener(date)
            }
        }

        override fun getItemCount() = nutritionList.size
    }


}