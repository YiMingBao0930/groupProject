package com.cs407.grouplab

import AddToLogDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
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


class FoodFragment : Fragment(), FoodItemAdapter.OnItemClickListener {

    private lateinit var searchView: SearchView
    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var suggestionsTextView : TextView
    private lateinit var addFoodButton: Button
    private lateinit var scanButton: ImageButton
    private val foodItemAdapter = FoodItemAdapter(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.food_search_view)
        foodRecyclerView = view.findViewById(R.id.food_recycler_view)
        noResultsTextView = view.findViewById(R.id.no_results_text_view)
        addFoodButton = view.findViewById(R.id.navigateToAddFood)
        scanButton = view.findViewById(R.id.jumptoscanpage)


        //Temporary database
        val foodDatabase = listOf(
            Food("Apple", 95, 0, 0),
            Food("Banana", 105, 0, 1),
            Food("Orange", 62, 0, 1),
            Food("Strawberry", 4, 0, 0),
            Food("Blueberries", 84, 0, 1),
            Food("Broccoli", 55, 0, 4),
            Food("Carrot", 25, 0, 1),
            Food("Potato", 163, 0, 4),
            Food("Chicken Breast", 165, 3, 31),
            Food("Egg", 78, 5, 6),
            Food("Almonds", 164, 14, 6),
            Food("Rice (Cooked)", 206, 0, 4),
            Food("Milk (1 cup)", 103, 2, 8),
            Food("Cheddar Cheese", 113, 9, 7),
            Food("Salmon (Cooked)", 206, 13, 22),
            Food("Beef Steak", 679, 48, 62),
            Food("Tofu", 94, 5, 10),
            Food("Peanut Butter (1 tbsp)", 94, 8, 4),
            Food("Yogurt (Plain)", 59, 0, 10),
            Food("Avocado", 240, 22, 3)
        )

        val consumedFoods = mapOf(
            "Apple" to 1,  // Ate 1 apple
            "Orange" to 2  // Ate 2 oranges
        )

        val result = calculateNutrients(foodDatabase, consumedFoods)

        val calories = result["Calories"] ?: 0  // Example value
        val fat = result["Fat"] ?: 0       // Example value
        val protein = result["Protein"] ?: 0      // Example value
        val totalCaloriesNeeded = 2000  // Example value based on user profile

// Call the function
        val suggestions = suggestCalorieIntake(calories, fat, protein, totalCaloriesNeeded)

// Display suggestions in a TextView (example)
        suggestionsTextView = view.findViewById(R.id.suggestionsTextView)
        suggestionsTextView.text = suggestions.joinToString("\n")



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
    }

    private fun performSearch(query: String) {
        val db = AppDatabase.getDatabase(requireContext())
        val liveDataResults = db.foodItemDao().searchFoodItems("%$query%")

        liveDataResults.observe(viewLifecycleOwner) { results ->
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

    override fun onItemClick(foodItem: FoodItem) {
        showAddToLogDialog(foodItem)
    }

    private fun showAddToLogDialog(foodItem: FoodItem) {
        val dialog = AddToLogDialogFragment(foodItem)
        dialog.show(parentFragmentManager, "AddToLogDialogFragment")
    }

    fun calculateNutrients(foodDatabase: List<Food>, consumedFoods: Map<String, Int>): Map<String, Int> {
        var totalCalories = 0
        var totalFat = 0
        var totalProtein = 0

        for ((foodName, quantity) in consumedFoods) {
            val food = foodDatabase.find { it.name == foodName }
            if (food != null) {
                totalCalories += food.calories * quantity
                totalFat += food.fat * quantity
                totalProtein += food.protein * quantity
            }
        }

        return mapOf(
            "Calories" to totalCalories,
            "Fat" to totalFat,
            "Protein" to totalProtein
        )
    }

}

