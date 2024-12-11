package com.cs407.grouplab

import AddToLogDialogFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.cs407.grouplab.data.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class Recommendation : Fragment() {
    private lateinit var db: AppDatabase
    private lateinit var userNutritionLogDao: UserNutritionLogDao
    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private lateinit var fatLight: ImageView
    private lateinit var carbLight: ImageView
    private lateinit var proteinLight: ImageView
    private lateinit var carbReview: TextView
    private lateinit var fatReview: TextView
    private lateinit var proteinReview: TextView
    private lateinit var sumEnergyFat: TextView
    private lateinit var sumEnergyProtein: TextView
    private lateinit var sumEnergyCarb: TextView
    private lateinit var suggestedFat: TextView
    private lateinit var suggestedProtein: TextView
    private lateinit var suggestedCarb: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        userNutritionLogDao = db.userNutritionLogDao()
    }

    private fun getCurrentUsername(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("logged_in_username", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recommendation, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch nutrition data and update UI
        fetchNutritionData(view)

        carbReview = view.findViewById(R.id.carbReview)
        proteinReview = view.findViewById(R.id.proteinReview)
        fatReview = view.findViewById(R.id.fatReview)
        fatLight = view.findViewById(R.id.fatLight)
        proteinLight = view.findViewById(R.id.proteinLight)
        carbLight = view.findViewById(R.id.carbLight)
        sumEnergyCarb = view.findViewById(R.id.sumEnergyCarb)
        sumEnergyFat = view.findViewById(R.id.sumEnergyFat)
        sumEnergyProtein = view.findViewById(R.id.sumEnergyProtein)
        suggestedFat = view.findViewById(R.id.suggestedFat)
        suggestedProtein = view.findViewById(R.id.suggestedProtein)
        suggestedCarb = view.findViewById(R.id.suggestedCarb)
        val backButton: ImageButton = view.findViewById(R.id.back_button)

        backButton.setOnClickListener {
            val fragment = FoodFragment() // Create an instance of the Recommendation fragment
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right, // Optional animations
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment) // Replace with the ID of your fragment container
                .addToBackStack(null) // Add to back stack to allow navigation back
                .commit()
        }

        loadUserStats(view)
    }

    private val stepUpdateHandler = android.os.Handler()
    private val stepUpdateRunnable = object : Runnable {
        override fun run() {
            val rootView = view ?: return
            stepUpdateHandler.postDelayed(this, 30000) // Update every 30 seconds
        }
    }

    override fun onStart() {
        super.onStart()
        stepUpdateHandler.post(stepUpdateRunnable)
    }

    override fun onStop() {
        super.onStop()
        stepUpdateHandler.removeCallbacks(stepUpdateRunnable)
    }



    private fun fetchNutritionData(view: View) {
        val username = getCurrentUsername()
        if (username == null) {
            // Handle case where user is not logged in
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dailyNutrition = userNutritionLogDao.getDailyNutrition(username, currentDate)

                withContext(Dispatchers.Main) {
                    if (dailyNutrition != null) {
                        // Update the pie chart with real data
                        val protein = dailyNutrition.totalProtein.toFloat()
                        val fat = dailyNutrition.totalFat.toFloat()
                        val carbs = dailyNutrition.totalCarbs.toFloat()

                        setupPieChart(view, protein, fat, carbs)

                        // Update calories
                    } else {
                        // Show default values if no data exists
                        setupPieChart(view, 0f, 0f, 0f)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Show error state
                    Toast.makeText(context, "Error loading nutrition data", Toast.LENGTH_SHORT).show()
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

    private fun loadUserStats(view: View) {
        val sharedPreferences =
            requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        if (username != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Retrieve user goals
                    val userGoal = db.userGoalDao().getUserGoal(username)
                    val recommendedCalories = userGoal?.dailyCalories ?: 2000

                    // Calculate recommended macros
                    val recommendedCarbs = (recommendedCalories * 0.3).toInt()
                    val recommendedCarbsMax = (recommendedCalories * 0.5).toInt()
                    val recommendedProtein = (recommendedCalories * 0.25).toInt()
                    val recommendedProteinMax = (recommendedCalories * 0.35).toInt()
                    val recommendedFat = (recommendedCalories * 0.2).toInt()
                    val recommendedFatMax = (recommendedCalories * 0.35).toInt()


                    // Retrieve daily nutrition log
                    val dailyNutrition =
                        db.userNutritionLogDao().getDailyNutrition(username, currentDate)

                    withContext(Dispatchers.Main) {
                        val totalCarbs = dailyNutrition.totalCarbs
                        val totalFat = dailyNutrition.totalFat
                        val totalProtein = dailyNutrition.totalProtein

                        sumEnergyFat.text = totalFat.toString()
                        sumEnergyProtein.text = dailyNutrition.totalProtein.toString()
                        sumEnergyCarb.text = dailyNutrition.totalCarbs.toString()
                        val suggestedFatMin = recommendedFat
                        val suggestedFatMax = recommendedFatMax
                        suggestedFat.text = "$suggestedFatMin - $suggestedFatMax"
                        val suggestedProteinMin = recommendedProtein
                        val suggestedProteinMax = recommendedProteinMax
                        suggestedProtein.text = "$suggestedProteinMin - $suggestedProteinMax"
                        val suggestedCarbMin = recommendedCarbs
                        val suggestedCarbMax = recommendedCarbsMax
                        suggestedCarb.text = "$suggestedCarbMin - $suggestedCarbMax"

                        when {
                            totalCarbs < recommendedCarbs -> {
                                carbReview.text = "A deficiency in carbohydrates may cause low blood sugar, dizziness, or even fainting, posing safety risks. It is suggested to consume more plain and low-oil staples like rice, steamed buns, mixed grain rice, or a variety of fresh fruits."
                                carbLight.setImageResource(R.drawable.ic_yellow_circle)
                            }
                            totalCarbs in recommendedCarbs..recommendedCarbsMax -> { // Use Int range
                                carbReview.text = "Moderate carbohydrate intake provides necessary energy for the body. Excellent!"
                                carbLight.setImageResource(R.drawable.ic_green_circle)
                            }
                            totalCarbs > recommendedCarbsMax -> {
                                carbReview.text = "Too many carbohydrates may result in a calorie surplus and slow down fat loss. Recommendation: Reduce consumption of sweets (including sugary drinks), candied fruits, or staples like rice and steamed buns."
                                carbLight.setImageResource(R.drawable.ic_red_circle)
                            }
                        }

                        when {
                            totalProtein < recommendedProtein -> {
                                proteinReview.text = "A lack of protein is detrimental to muscle maintenance and repair, which may slow weight loss and lead to quicker rebound. It is advised to eat more lean, lightly stir-fried meats, seafood such as fish, shrimp, and shellfish, or tofu products."
                                proteinLight.setImageResource(R.drawable.ic_yellow_circle)
                            }
                            totalProtein in recommendedProtein..recommendedProteinMax -> { // Use Int range
                                proteinReview.text = "Reasonable protein intake helps maintain and repair muscles. Well done!"
                                proteinLight.setImageResource(R.drawable.ic_green_circle)
                            }
                            totalProtein > recommendedProteinMax-> {
                                proteinReview.text = "Excess protein intake may cause nutritional imbalance. Recommendation: Eat less lean meat, dairy products, seafood, or tofu."
                                proteinLight.setImageResource(R.drawable.ic_red_circle)
                            }
                        }

                        when {
                            totalFat < recommendedFat -> {
                                fatReview.text = "Insufficient fat intake can lead to a lack of essential fatty acids. It is recommended to snack on a handful of plain nuts or have half an avocado as a supplement."
                                fatLight.setImageResource(R.drawable.ic_yellow_circle)
                            }
                            totalFat in recommendedFat..recommendedFatMax -> { // Use Int range
                                fatReview.text = "Adequate fat intake provides essential high-quality fatty acids. Great job!"
                                fatLight.setImageResource(R.drawable.ic_green_circle)
                            }
                            totalFat > recommendedFatMax-> {
                                fatReview.text = "Excess fat intake can lead to calorie surplus, hinder fat loss, and even cause weight gain. Recommendation: Avoid fried or grilled foods, fatty meats, crispy biscuits, etc."
                                fatLight.setImageResource(R.drawable.ic_red_circle)
                            }
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}