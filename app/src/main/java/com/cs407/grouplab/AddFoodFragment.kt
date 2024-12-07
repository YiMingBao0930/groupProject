package com.cs407.grouplab

import AddToLogDialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.data.AppDatabase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFoodFragment : Fragment() {

    private lateinit var foodNameInput: TextInputEditText
    private lateinit var caloriesInput: TextInputEditText
    private lateinit var proteinInput: TextInputEditText
    private lateinit var carbsInput: TextInputEditText
    private lateinit var fatInput: TextInputEditText
    private lateinit var satFatInput: TextInputEditText
    private lateinit var transFatInput: TextInputEditText
    private lateinit var polyFatInput: TextInputEditText
    private lateinit var monoFatInput: TextInputEditText
    private lateinit var cholesterolInput: TextInputEditText
    private lateinit var sodiumInput: TextInputEditText
    private lateinit var potassiumInput: TextInputEditText
    private lateinit var fiberInput: TextInputEditText
    private lateinit var sugarInput: TextInputEditText
    private lateinit var vitaminAInput: TextInputEditText
    private lateinit var vitaminBInput: TextInputEditText
    private lateinit var vitaminCInput: TextInputEditText
    private lateinit var vitaminDInput: TextInputEditText
    private lateinit var calciumInput: TextInputEditText
    private lateinit var ironInput: TextInputEditText

    private lateinit var saveFoodButton: Button
    private lateinit var foodAdapter: FoodItemAdapter
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_food, container, false)

        // Initialize inputs
        foodNameInput = view.findViewById(R.id.food_name_input)
        caloriesInput = view.findViewById(R.id.calories_input)
        proteinInput = view.findViewById(R.id.protein_input)
        carbsInput = view.findViewById(R.id.carbs_input)
        fatInput = view.findViewById(R.id.fat_input)
        satFatInput = view.findViewById(R.id.sat_fat_input)
        transFatInput = view.findViewById(R.id.trans_fat_input)
        polyFatInput = view.findViewById(R.id.poly_fat_input)
        monoFatInput = view.findViewById(R.id.mono_fat_input)
        cholesterolInput = view.findViewById(R.id.cholesterol_input)
        sodiumInput = view.findViewById(R.id.sodium_input)
        potassiumInput = view.findViewById(R.id.potassium_input)
        fiberInput = view.findViewById(R.id.fiber_input)
        sugarInput = view.findViewById(R.id.sugar_input)
        vitaminAInput = view.findViewById(R.id.vitamina_input)
        vitaminBInput = view.findViewById(R.id.vitaminb_input)
        vitaminCInput = view.findViewById(R.id.vitaminc_input)
        vitaminDInput = view.findViewById(R.id.vitamind_input)
        calciumInput = view.findViewById(R.id.calcium_input)
        ironInput = view.findViewById(R.id.iron_input)

        saveFoodButton = view.findViewById(R.id.save_food_button)

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.add_food_toolbar)
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

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.food_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        foodAdapter = FoodItemAdapter(object : FoodItemAdapter.OnItemClickListener {
            override fun onItemClick(foodItem: FoodItem) {
                showAddToLogDialog(foodItem)
            }
        })
        recyclerView.adapter = foodAdapter

        // Initialize SearchView
        searchView = view.findViewById(R.id.search_view)
        setupSearch()

        // Load initial data
        loadFoodItems("")

        // Set up save button listener
        saveFoodButton.setOnClickListener {
            if (validateInputs()) {
                saveFood()
            } else {
                Snackbar.make(view, "Please fill in all required fields.", Snackbar.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate required fields
        if (foodNameInput.text.isNullOrEmpty()) {
            foodNameInput.error = "Food name is required"
            isValid = false
        }
        if (caloriesInput.text.isNullOrEmpty()) {
            caloriesInput.error = "Calories are required"
            isValid = false
        }
        if (proteinInput.text.isNullOrEmpty()) {
            proteinInput.error = "Protein is required"
            isValid = false
        }
        if (carbsInput.text.isNullOrEmpty()) {
            carbsInput.error = "Carbs are required"
            isValid = false
        }
        if (fatInput.text.isNullOrEmpty()) {
            fatInput.error = "Fat is required"
            isValid = false
        }

        return isValid
    }

    private fun saveFood() {
        // Extract values from inputs
        val foodName = foodNameInput.text.toString().trim()
        val calories = caloriesInput.text.toString().toIntOrNull() ?: 0
        val protein = proteinInput.text.toString().toIntOrNull() ?: 0
        val carbs = carbsInput.text.toString().toIntOrNull() ?: 0
        val fat = fatInput.text.toString().toIntOrNull() ?: 0
        val satFat = satFatInput.text.toString().toIntOrNull() ?: 0
        val transFat = transFatInput.text.toString().toIntOrNull() ?: 0
        val polyFat = polyFatInput.text.toString().toIntOrNull() ?: 0
        val monoFat = monoFatInput.text.toString().toIntOrNull() ?: 0
        val cholesterol = cholesterolInput.text.toString().toIntOrNull() ?: 0
        val sodium = sodiumInput.text.toString().toIntOrNull() ?: 0
        val potassium = potassiumInput.text.toString().toIntOrNull() ?: 0
        val fiber = fiberInput.text.toString().toIntOrNull() ?: 0
        val sugar = sugarInput.text.toString().toIntOrNull() ?: 0
        val vitaminA = vitaminAInput.text.toString().toIntOrNull() ?: 0
        val vitaminB = vitaminBInput.text.toString().toIntOrNull() ?: 0
        val vitaminC = vitaminCInput.text.toString().toIntOrNull() ?: 0
        val vitaminD = vitaminDInput.text.toString().toIntOrNull() ?: 0
        val calcium = calciumInput.text.toString().toIntOrNull() ?: 0
        val iron = ironInput.text.toString().toIntOrNull() ?: 0

        // Validate food name
        if (foodName.isEmpty()) {
            Snackbar.make(requireView(), "Food name cannot be empty", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Create a FoodItem object
        val foodItem = FoodItem(
            name = foodName,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            saturatedFat = satFat,
            transFat = transFat,
            polyUnsaturatedFat = polyFat,
            monoUnsaturatedFat = monoFat,
            cholesterol = cholesterol,
            sodium = sodium,
            potassium = potassium,
            fiber = fiber,
            sugar = sugar,
            vitaminA = vitaminA,
            vitaminB = vitaminB,
            vitaminC = vitaminC,
            vitaminD = vitaminD,
            calcium = calcium,
            iron = iron
        )

        // Save the food item to the database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                db.foodItemDao().insert(foodItem)
                CoroutineScope(Dispatchers.Main).launch {
                    Snackbar.make(requireView(), "Food saved successfully!", Snackbar.LENGTH_SHORT).show()
                    // Navigate back or clear fields
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Snackbar.make(requireView(), "Error saving food: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    Log.e("Error", "Error saving food: ${e.message}")
                }
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                loadFoodItems(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loadFoodItems(newText ?: "")
                return true
            }
        })
    }

    private fun loadFoodItems(query: String) {
        val searchQuery = "%$query%"
        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            db.foodItemDao().searchFoodItems(searchQuery).collect { foodItems ->
                foodAdapter.setItems(foodItems)
            }
        }
    }

    private fun showAddToLogDialog(foodItem: FoodItem) {
        val dialog = AddToLogDialogFragment(foodItem)
        dialog.show(parentFragmentManager, "AddToLogDialog")
    }

}
