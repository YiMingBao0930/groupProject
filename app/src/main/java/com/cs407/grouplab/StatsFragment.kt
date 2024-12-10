package com.cs407.grouplab

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.grouplab.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsFragment : Fragment() {
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.exercise_toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button in the toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "" // Hide the toolbar title text
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back to the previous fragment
        }

        loadUserStats(view)
    }

    private fun loadUserStats(view: View) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        if (username != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Retrieve user goals
                    val userGoal = db.userGoalDao().getUserGoal(username)
                    val recommendedCalories = userGoal?.dailyCalories ?: 2000

                    // Calculate recommended macros
                    val recommendedCarbs = userGoal?.carbGram ?: (recommendedCalories * 0.5 / 4).toInt() // 50% from carbs
                    val recommendedProtein = userGoal?.proteinGram ?: (recommendedCalories * 0.3 / 4).toInt() // 30% from protein
                    val recommendedFat = userGoal?.fatGram ?: (recommendedCalories * 0.25 / 9).toInt() // 25% from fat
                    val recommendedFiber = 25
                    val recommendedSugar = 50
                    val recommendedVitaminA = 900 // in mcg
                    val recommendedVitaminB = 1.3 // in mg
                    val recommendedVitaminC = 90 // in mg
                    val recommendedVitaminD = 15 // in mcg


                    // Retrieve daily nutrition log
                    val dailyNutrition = db.userNutritionLogDao().getDailyNutrition(username, currentDate)

                    withContext(Dispatchers.Main) {
                        // Update calories display
                        val caloriesSum = view.findViewById<TextView>(R.id.calories_sum)
                        caloriesSum.text = "${dailyNutrition.totalCalories}"

                        // Calories Progress
                        val distanceProgressBar = view.findViewById<ProgressBar>(R.id.distance_progress_bar)
                        distanceProgressBar.max = recommendedCalories
                        ObjectAnimator.ofInt(distanceProgressBar, "progress", dailyNutrition.totalCalories)
                            .setDuration(2000)
                            .start()
                        val distanceText = view.findViewById<TextView>(R.id.distance_text)
                        distanceText.text = "Calories: ${dailyNutrition.totalCalories} / $recommendedCalories"

                        // Protein Progress
                        val proteinProgressBar = view.findViewById<ProgressBar>(R.id.protein_progress_bar)
                        proteinProgressBar.max = recommendedProtein
                        ObjectAnimator.ofInt(proteinProgressBar, "progress", dailyNutrition.totalProtein)
                            .setDuration(2000)
                            .start()
                        val proteinText = view.findViewById<TextView>(R.id.protein_text)
                        proteinText.text = "Protein: ${dailyNutrition.totalProtein}g / ${recommendedProtein}g"

                        // Fat Progress
                        val fatProgressBar = view.findViewById<ProgressBar>(R.id.fat_progress_bar)
                        fatProgressBar.max = recommendedFat
                        ObjectAnimator.ofInt(fatProgressBar, "progress", dailyNutrition.totalFat)
                            .setDuration(2000)
                            .start()
                        val fatText = view.findViewById<TextView>(R.id.fat_text)
                        fatText.text = "Fat: ${dailyNutrition.totalFat}g / ${recommendedFat}g"

                        // Carbs Progress
                        val carbsProgressBar = view.findViewById<ProgressBar>(R.id.carbs_progress_bar)
                        carbsProgressBar.max = recommendedCarbs
                        ObjectAnimator.ofInt(carbsProgressBar, "progress", dailyNutrition.totalCarbs)
                            .setDuration(2000)
                            .start()
                        val carbsText = view.findViewById<TextView>(R.id.carbs_text)
                        carbsText.text = "Carbs: ${dailyNutrition.totalCarbs}g / ${recommendedCarbs}g"

                        // Update Fiber
                        val fiberProgressBar = view.findViewById<ProgressBar>(R.id.fiber_progress_bar)
                        fiberProgressBar.max = recommendedFiber
                        ObjectAnimator.ofInt(fiberProgressBar, "progress", dailyNutrition.totalFiber)
                            .setDuration(2000)
                            .start()
                        val fiberText = view.findViewById<TextView>(R.id.fiber_text)
                        fiberText.text = "Fiber: ${dailyNutrition.totalFiber}g / ${recommendedFiber}g"

                        // Update Sugar
                        val sugarProgressBar = view.findViewById<ProgressBar>(R.id.sugar_progress_bar)
                        sugarProgressBar.max = recommendedSugar
                        ObjectAnimator.ofInt(sugarProgressBar, "progress", dailyNutrition.totalSugar)
                            .setDuration(2000)
                            .start()
                        val sugarText = view.findViewById<TextView>(R.id.sugar_text)
                        sugarText.text = "Sugar: ${dailyNutrition.totalSugar}g / ${recommendedSugar}g"

                        // Trans Fat Progress
                        val transFatProgressBar = view.findViewById<ProgressBar>(R.id.trans_fat_progress_bar)
                        transFatProgressBar.max = (0.01 * recommendedCalories.toDouble()).toInt() // There is no recommended intake for trans fat
                        ObjectAnimator.ofInt(transFatProgressBar, "progress", dailyNutrition.totalTransFat)
                            .setDuration(2000)
                            .start()
                        val transFatText = view.findViewById<TextView>(R.id.trans_fat_text)
                        transFatText.text = "Trans Fat: ${dailyNutrition.totalTransFat}g / ${transFatProgressBar.max}g"

                        // Polyunsaturated Fat Progress
                        val polyFatProgressBar = view.findViewById<ProgressBar>(R.id.polyunsaturated_fat_progress_bar)
                        polyFatProgressBar.max = 25 // Example: 25g recommended
                        ObjectAnimator.ofInt(polyFatProgressBar, "progress", dailyNutrition.totalPolySaturatedFat)
                            .setDuration(2000)
                            .start()
                        val polyFatText = view.findViewById<TextView>(R.id.polyunsaturated_fat_text)
                        polyFatText.text = "Polyunsaturated Fat: ${dailyNutrition.totalPolySaturatedFat}g / 25g"

                        // Monounsaturated Fat Progress
                        val monoFatProgressBar = view.findViewById<ProgressBar>(R.id.monounsaturated_fat_progress_bar)
                        monoFatProgressBar.max = 20 // Example: 20g recommended
                        ObjectAnimator.ofInt(monoFatProgressBar, "progress", dailyNutrition.totalMonoSaturatedFat)
                            .setDuration(2000)
                            .start()
                        val monoFatText = view.findViewById<TextView>(R.id.monounsaturated_fat_text)
                        monoFatText.text = "Monounsaturated Fat: ${dailyNutrition.totalMonoSaturatedFat}g / 20g"

                        // Update Sodium
                        val sodiumProgressBar = view.findViewById<ProgressBar>(R.id.sodium_progress_bar)
                        sodiumProgressBar.max = 2300 // Recommended sodium intake in mg
                        ObjectAnimator.ofInt(sodiumProgressBar, "progress", dailyNutrition.totalSodium)
                            .setDuration(2000)
                            .start()
                        val sodiumText = view.findViewById<TextView>(R.id.sodium_text)
                        sodiumText.text = "Sodium: ${dailyNutrition.totalSodium}mg / 2300mg"

                        // Update Potassium
                        val potassiumProgressBar = view.findViewById<ProgressBar>(R.id.potassium_progress_bar)
                        potassiumProgressBar.max = 3500 // Recommended potassium intake in mg
                        ObjectAnimator.ofInt(potassiumProgressBar, "progress", dailyNutrition.totalPotassium)
                            .setDuration(2000)
                            .start()
                        val potassiumText = view.findViewById<TextView>(R.id.potassium_text)
                        potassiumText.text = "Potassium: ${dailyNutrition.totalPotassium}mg / 3500mg"

                        // Update Calcium
                        val calciumProgressBar = view.findViewById<ProgressBar>(R.id.calcium_progress_bar)
                        calciumProgressBar.max = 1000
                        ObjectAnimator.ofInt(calciumProgressBar, "progress", dailyNutrition.totalCalcium)
                            .setDuration(2000)
                            .start()
                        val calciumText = view.findViewById<TextView>(R.id.calcium_text)
                        calciumText.text = "Calcium: ${dailyNutrition.totalCalcium}mg / ${1000}mg"

                        // Update Iron
                        val ironProgressBar = view.findViewById<ProgressBar>(R.id.iron_progress_bar)
                        ironProgressBar.max = 18
                        ObjectAnimator.ofInt(ironProgressBar, "progress", dailyNutrition.totalIron)
                            .setDuration(2000)
                            .start()
                        val ironText = view.findViewById<TextView>(R.id.iron_text)
                        ironText.text = "Iron: ${dailyNutrition.totalIron}mg / ${18}mg"

                        // Update Vitamin A
                        val vitaminAProgressBar = view.findViewById<ProgressBar>(R.id.vitamin_a_progress_bar)
                        vitaminAProgressBar.max = recommendedVitaminA
                        ObjectAnimator.ofInt(vitaminAProgressBar, "progress", dailyNutrition.totalVitaminA)
                            .setDuration(2000)
                            .start()
                        val vitaminAText = view.findViewById<TextView>(R.id.vitamin_a_text)
                        vitaminAText.text = "Vitamin A: ${dailyNutrition.totalVitaminA}mcg / ${recommendedVitaminA}mcg"

                        // Update Vitamin B
                        val vitaminBProgressBar = view.findViewById<ProgressBar>(R.id.vitamin_b_progress_bar)
                        vitaminBProgressBar.max = (recommendedVitaminB * 100).toInt() // Convert mg to a more intuitive scale
                        ObjectAnimator.ofInt(vitaminBProgressBar, "progress", (dailyNutrition.totalVitaminB * 100).toInt())
                            .setDuration(2000)
                            .start()
                        val vitaminBText = view.findViewById<TextView>(R.id.vitamin_b_text)
                        vitaminBText.text = "Vitamin B: ${dailyNutrition.totalVitaminB}mg / ${recommendedVitaminB}mg"

                        // Update Vitamin C
                        val vitaminCProgressBar = view.findViewById<ProgressBar>(R.id.vitamin_c_progress_bar)
                        vitaminCProgressBar.max = recommendedVitaminC
                        ObjectAnimator.ofInt(vitaminCProgressBar, "progress", dailyNutrition.totalVitaminC)
                            .setDuration(2000)
                            .start()
                        val vitaminCText = view.findViewById<TextView>(R.id.vitamin_c_text)
                        vitaminCText.text = "Vitamin C: ${dailyNutrition.totalVitaminC}mg / ${recommendedVitaminC}mg"

                        // Update Vitamin D
                        val vitaminDProgressBar = view.findViewById<ProgressBar>(R.id.vitamin_d_progress_bar)
                        vitaminDProgressBar.max = recommendedVitaminD
                        ObjectAnimator.ofInt(vitaminDProgressBar, "progress", dailyNutrition.totalVitaminD)
                            .setDuration(2000)
                            .start()
                        val vitaminDText = view.findViewById<TextView>(R.id.vitamin_d_text)
                        vitaminDText.text = "Vitamin D: ${dailyNutrition.totalVitaminD}mcg / ${recommendedVitaminD}mcg"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


}