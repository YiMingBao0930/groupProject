package com.cs407.grouplab

import AddToLogDialogFragment
import android.animation.ObjectAnimator
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
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.data.AppDatabase
import com.cs407.grouplab.data.Food
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import suggestCalorieIntake
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class FoodFragment : Fragment(), FoodItemAdapter.OnItemClickListener {
    private lateinit var db: AppDatabase

    private lateinit var searchView: SearchView
    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var suggestionsTextView : TextView
    private lateinit var addFoodButton: Button
    private lateinit var scanButton: ImageButton
    private val foodItemAdapter = FoodItemAdapter(this)
    private lateinit var fatLight: ImageView
    private lateinit var carbLight: ImageView
    private lateinit var proteinLight: ImageView
    private lateinit var carbReview: TextView
    private lateinit var fatReview: TextView
    private lateinit var proteinReview: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        // Find the TextView by its ID


        // Set an onClickListener


        searchView = view.findViewById(R.id.food_search_view)
        foodRecyclerView = view.findViewById(R.id.food_recycler_view)
        noResultsTextView = view.findViewById(R.id.no_results_text_view)
        addFoodButton = view.findViewById(R.id.navigateToAddFood)
        scanButton = view.findViewById(R.id.jumptoscanpage)
        carbReview = view.findViewById(R.id.carbReview)
        proteinReview = view.findViewById(R.id.proteinReview)
        fatReview = view.findViewById(R.id.fatReview)
        fatLight = view.findViewById(R.id.fatLight)
        proteinLight = view.findViewById(R.id.proteinLight)
        carbLight = view.findViewById(R.id.carbLight)




        foodRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        foodRecyclerView.adapter = foodItemAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { performSearch(it) }
                return true
            }
        })

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_food)
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

        // Navigate to AddFoodFragment
        addFoodButton.setOnClickListener {
            val fragment = AddFoodFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        scanButton.setOnClickListener {
            val fragment = ScanPageFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                 R.anim.slide_out_left,
                 R.anim.slide_in_left,
                    R.anim.slide_out_right
            )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        loadUserStats(view)
    }

    private fun performSearch(query: String) {
        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            db.foodItemDao().searchFoodItems("%$query%").collect { results ->
                if (results.isNullOrEmpty()) {
                    noResultsTextView.visibility = View.VISIBLE
                    foodRecyclerView.visibility = View.GONE
                } else {
                    noResultsTextView.visibility = View.GONE
                    foodRecyclerView.visibility = View.VISIBLE
                    foodItemAdapter.setItems(results)
                }
            }
        }
    }

    override fun onItemClick(foodItem: FoodItem) {
        showAddToLogDialog(foodItem)
    }

    private fun showAddToLogDialog(foodItem: FoodItem) {
        val dialog = AddToLogDialogFragment(foodItem)
        dialog.show(parentFragmentManager, "AddToLogDialogFragment")
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
                    val recommendedCarbs = userGoal?.carbGram ?: (recommendedCalories * 0.5 / 4).toInt() // 50% from carbs
                    val recommendedCarbsMax = userGoal?.carbGram ?: (recommendedCalories * 0.65 / 4).toInt() // 65% from carbs
                    val recommendedProtein = userGoal?.proteinGram ?: (recommendedCalories * 0.1 / 4).toInt() // 10% from protein
                    val recommendedProteinMax = userGoal?.proteinGram ?: (recommendedCalories * 0.35 / 4).toInt() // 35% from protein
                    val recommendedFat = userGoal?.fatGram ?: (recommendedCalories * 0.2 / 9).toInt() // 20% from fat
                    val recommendedFatMax = userGoal?.fatGram ?: (recommendedCalories * 0.35 / 9).toInt() // 35% from fat


                    // Retrieve daily nutrition log
                    val dailyNutrition =
                        db.userNutritionLogDao().getDailyNutrition(username, currentDate)

                    withContext(Dispatchers.Main) {
                        val totalCarbs = dailyNutrition.totalCarbs
                        val totalFat = dailyNutrition.totalFat
                        val totalProtein = dailyNutrition.totalProtein

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

