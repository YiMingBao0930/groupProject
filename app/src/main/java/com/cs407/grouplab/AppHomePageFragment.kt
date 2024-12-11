package com.cs407.grouplab

import AddToLogDialogFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import androidx.appcompat.app.AlertDialog

class AppHomePageFragment : Fragment() {
    private lateinit var db: AppDatabase
    private lateinit var userNutritionLogDao: UserNutritionLogDao
    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var cachedMealType: String? = null
    private var cachedRecommendation: MealRecommendation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        userNutritionLogDao = db.userNutritionLogDao()
    }

    private fun getCurrentUsername(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("logged_in_username", null)
    }

    private fun getCurrentMealType(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..10 -> "Breakfast"
            in 11..13 -> "Lunch"
            in 14..16 -> "Snack"
            in 17..20 -> "Dinner"
            else -> "LateMeal"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_page, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch nutrition data and update UI
        fetchNutritionData(view)
        displayRecommendations(view)
        updateStepsUI(view)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_food -> {
                    val fragment = FoodFragment() // Replace with your FoodFragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_calendar -> {
                    val fragment = DailySummaryFragment() // Replace with your DailySummaryFragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_walk -> {
                    val fragment = ExerciseMenuFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_home -> {
                    val fragment = AppHomePageFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }
                R.id.nav_stats -> {
                    val fragment = StatsFragment() // Replace with your StatsFragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_review -> {
                    val fragment = Recommendation()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }


        val navigationView =
            view.findViewById<com.google.android.material.navigation.NavigationView>(R.id.navigationView)
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.yourGoals -> {
                    val fragment = GoalSettingFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.logout -> {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes") { _, _ ->
                            val sharedPreferences = requireContext()
                                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().apply {
                                remove("logged_in_username")
                                apply()
                            }

                            parentFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_right,
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_right
                                )
                                .replace(R.id.fragment_container, LoginFragment())
                                .commit()

                            Toast.makeText(
                                requireContext(),
                                "Logged out successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .setNegativeButton("No", null)
                        .show()

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.reminders -> {
                    val fragment = RemindersFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    // Add a debug Toast to see which menu item was clicked
                    Toast.makeText(requireContext(), "Clicked: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
            }
        }

        val settingButton: ImageView = view.findViewById(R.id.settingButton)
        settingButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private val stepUpdateHandler = android.os.Handler()
    private val stepUpdateRunnable = object : Runnable {
        override fun run() {
            val rootView = view ?: return
            updateStepsUI(rootView)
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

    private fun displayRecommendations(view: View) {
        val username = getCurrentUsername()
        if (username == null) {
            // Handle case where user is not logged in
            return
        }

        val currentMealType = getCurrentMealType()

        // Check if the recommendation is already cached for the current meal type
        if (cachedMealType == currentMealType && cachedRecommendation != null) {
            updateRecommendationUI(view, cachedRecommendation)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get the meal recommendation
                val recommendation = recommendMealByTime(
                    username = username,
                    foodItemDao = db.foodItemDao(),
                    userGoalDao = db.userGoalDao(),
                    userNutritionLogDao = db.userNutritionLogDao()
                )

                // Cache the recommendation and meal type
                cachedMealType = currentMealType
                cachedRecommendation = recommendation

                withContext(Dispatchers.Main) {
                    updateRecommendationUI(view, recommendation)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading recommendations", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateStepsUI(view: View) {
        val username = getCurrentUsername() ?: return
        val stepsTextView = view.findViewById<TextView>(R.id.steps_text)
        val stepsProgressBar = view.findViewById<ProgressBar>(R.id.steps_progress_bar)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val userGoalDao = db.userGoalDao()
                val stepRecordDao = db.stepRecordDao()

                val userGoal = userGoalDao.getUserGoal(username)
                val stepRecord = stepRecordDao.getStepRecordForDate(username, currentDate)

                val stepsGoal = userGoal?.stepsGoal ?: 10000
                val steps = stepRecord?.steps ?: 0

                withContext(Dispatchers.Main) {
                    stepsTextView.text = "Steps: $steps/$stepsGoal"
                    stepsProgressBar.max = stepsGoal
                    stepsProgressBar.progress = steps
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error fetching step data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun updateRecommendationUI(view: View, recommendation: MealRecommendation?) {
        val titleTextView = view.findViewById<TextView>(R.id.recommendation_title)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recommendation_recycler_view)

        if (recommendation != null) {
            // Update the recommendation title with the meal type
            titleTextView.text = "${recommendation.mealType} Recommendations"

            // Update the RecyclerView with recommended foods and servings
            recyclerView.adapter = RecommendationAdapter(recommendation.foods) { foodRecommendation ->
                showAddToLogDialog(foodRecommendation.food) // Handle item click
            }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        } else {
            // No recommendations available
            titleTextView.text = "No Recommendations Available"
            recyclerView.adapter = RecommendationAdapter(emptyList(), {})
        }
    }

    private fun showAddToLogDialog(foodItem: FoodItem) {
        val dialog = AddToLogDialogFragment(foodItem)
        dialog.show(childFragmentManager, "AddToLogDialog")
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

                        // Update the nutrition text views
                        view.findViewById<TextView>(R.id.protein_num).text = "${protein.toInt()}g\n"+ "Protein"
                        view.findViewById<TextView>(R.id.fat_num).text = "${fat.toInt()}g\n"+ "Fat"
                        view.findViewById<TextView>(R.id.acrbs_num).text = "${carbs.toInt()}g\n"+ "Carbs"

                        // Update calories
                        val totalCalories = dailyNutrition.totalCalories
                        val userGoal = db.userGoalDao().getUserGoal(username)
                        val goalCalories = userGoal?.dailyCalories ?: 2000
                        view.findViewById<TextView>(R.id.calorie_num).text =
                            "$totalCalories/$goalCalories kCal"
                    } else {
                        // Show default values if no data exists
                        setupPieChart(view, 0f, 0f, 0f)
                        // Update text views to show 0
                        view.findViewById<TextView>(R.id.protein_num).text = "0g\n" + "Protein"
                        view.findViewById<TextView>(R.id.fat_num).text = "0g\n" + "Fat"
                        view.findViewById<TextView>(R.id.acrbs_num).text = "0g\n" + "Carbs"
                        view.findViewById<TextView>(R.id.calorie_num).text = "0/2000 kCal"
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
}
