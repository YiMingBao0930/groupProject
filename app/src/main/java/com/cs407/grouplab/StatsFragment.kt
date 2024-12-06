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

        // Back button to navigate to AppHomePageFragment
        val lastPageButton: ImageButton = view.findViewById(R.id.back_button)
        lastPageButton.setOnClickListener {
            val fragment = AppHomePageFragment()
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
                    val dailyNutrition = db.userNutritionLogDao().getDailyNutrition(username, currentDate)

                    withContext(Dispatchers.Main) {
                        // Update calories display
                        val caloriesSum = view.findViewById<TextView>(R.id.calories_sum)
                        caloriesSum.text = "${dailyNutrition.totalCalories}"

                        // Distance progress bar (repurposed for total calories progress)
                        val distanceProgressBar = view.findViewById<ProgressBar>(R.id.distance_progress_bar)
                        val recommendedCalories = sharedPreferences.getInt("recommended_calories", 2000)
                        distanceProgressBar.max = recommendedCalories
                        val caloriesProgress = dailyNutrition.totalCalories
                        ObjectAnimator.ofInt(distanceProgressBar, "progress", caloriesProgress)
                            .setDuration(2000)
                            .start()
                        val distanceText = view.findViewById<TextView>(R.id.distance_text)
                        distanceText.text = "Calories: $caloriesProgress / $recommendedCalories"

                        // Protein progress bar
                        val proteinProgressBar = view.findViewById<ProgressBar>(R.id.protein_progress_bar)
                        val recommendedProtein = (recommendedCalories * 0.3 / 4).toInt() // 30% of calories from protein
                        proteinProgressBar.max = recommendedProtein
                        val proteinProgress = dailyNutrition.totalProtein
                        ObjectAnimator.ofInt(proteinProgressBar, "progress", proteinProgress)
                            .setDuration(2000)
                            .start()
                        val proteinText = view.findViewById<TextView>(R.id.protein_text)
                        proteinText.text = "Protein: ${proteinProgress}g / ${recommendedProtein}g"

                        // Fat progress bar
                        val fatProgressBar = view.findViewById<ProgressBar>(R.id.fat_progress_bar)
                        val recommendedFat = (recommendedCalories * 0.25 / 9).toInt() // 25% of calories from fat
                        fatProgressBar.max = recommendedFat
                        val fatProgress = dailyNutrition.totalFat
                        ObjectAnimator.ofInt(fatProgressBar, "progress", fatProgress)
                            .setDuration(2000)
                            .start()
                        val fatText = view.findViewById<TextView>(R.id.fat_text)
                        fatText.text = "Fat: ${fatProgress}g / ${recommendedFat}g"

                        // Update trans fat progress
                        val transFatProgressBar = view.findViewById<ProgressBar>(R.id.trans_fat_progress_bar)
                        transFatProgressBar.max = recommendedFat
                        ObjectAnimator.ofInt(transFatProgressBar, "progress", dailyNutrition.totalTransFat)
                            .setDuration(2000)
                            .start()

                        // Update polyunsaturated fat progress
                        val polyFatProgressBar = view.findViewById<ProgressBar>(R.id.polyunsaturated_fat_progress_bar)
                        polyFatProgressBar.max = recommendedFat
                        ObjectAnimator.ofInt(polyFatProgressBar, "progress", dailyNutrition.totalPolySaturatedFat)
                            .setDuration(2000)
                            .start()

                        // Update monounsaturated fat progress
                        val monoFatProgressBar = view.findViewById<ProgressBar>(R.id.monounsaturated_fat_progress_bar)
                        monoFatProgressBar.max = recommendedFat
                        ObjectAnimator.ofInt(monoFatProgressBar, "progress", dailyNutrition.totalMonoSaturatedFat)
                            .setDuration(2000)
                            .start()

                        // Update sodium progress
                        val sodiumProgressBar = view.findViewById<ProgressBar>(R.id.sodium_progress_bar)
                        sodiumProgressBar.max = 2300 // Recommended daily sodium intake in mg
                        ObjectAnimator.ofInt(sodiumProgressBar, "progress", dailyNutrition.totalSodium)
                            .setDuration(2000)
                            .start()

                        // Update potassium progress
                        val potassiumProgressBar = view.findViewById<ProgressBar>(R.id.potassium_progress_bar)
                        potassiumProgressBar.max = 3500 // Recommended daily potassium intake in mg
                        ObjectAnimator.ofInt(potassiumProgressBar, "progress", dailyNutrition.totalPotassium)
                            .setDuration(2000)
                            .start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}