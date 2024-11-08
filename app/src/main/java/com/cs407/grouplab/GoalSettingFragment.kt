package com.cs407.grouplab

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import kotlin.math.roundToInt
import java.util.Calendar


class GoalSettingFragment : Fragment() {

    private lateinit var proteinSlider: Slider
    private lateinit var fatSlider: Slider
    private lateinit var carbsSlider: Slider
    private lateinit var proteinPTextView: TextView
    private lateinit var proteinGTextView: TextView
    private lateinit var fatPTextView: TextView
    private lateinit var fatGTextView: TextView
    private lateinit var carbsPTextView: TextView
    private lateinit var carbsGTextView: TextView
    private lateinit var curWeightTextEdit: TextView
    private lateinit var goalWeightTextEdit: TextView
    private lateinit var recommendedCaloriesTextEdit: TextView
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var goalDateButton: Button


    // Example base caloric intake
    private var baseCalories = 2000
    private var isAdjusting = false // Prevent recursive updates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_goal_setting, container, false)

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar3)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "" // Hide the toolbar title text
        }
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back to the previous fragment
        }

        // Initialize views
        proteinSlider = view.findViewById(R.id.proteinSlider)
        fatSlider = view.findViewById(R.id.fatSlider)
        carbsSlider = view.findViewById(R.id.carbsSlider)
        proteinPTextView = view.findViewById(R.id.proteinPTextView)
        proteinGTextView = view.findViewById(R.id.proteinGTextView)
        fatPTextView = view.findViewById(R.id.fatPTextView)
        fatGTextView = view.findViewById(R.id.fatGTextView)
        carbsPTextView = view.findViewById(R.id.carbsPTextView)
        carbsGTextView = view.findViewById(R.id.carbsGTextView)
        curWeightTextEdit = view.findViewById(R.id.curWeightTextEdit)
        goalWeightTextEdit = view.findViewById(R.id.goalWeightTextEdit)
        recommendedCaloriesTextEdit = view.findViewById(R.id.recommendedCaloriesTextEdit)
        activityLevelSpinner = view.findViewById(R.id.activityLevelSpinner)

        goalDateButton = view.findViewById(R.id.goalDate_button)

        // Set up the DatePickerDialog
        goalDateButton.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up the activity level spinner
        val activityLevels = arrayOf(
            "Sedentary (little or no exercise)",
            "Lightly Active (exercise 1-3 days/week)",
            "Moderately Active (exercise 3-5 days/week)",
            "Very Active (exercise 6-7 days/week)"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, // Use the custom layout
            activityLevels
        )
        adapter.setDropDownViewResource(R.layout.dropdown_item) // Custom layout for dropdown items
        activityLevelSpinner.adapter = adapter

        // Set up listeners for sliders
        proteinSlider.addOnChangeListener { _, value, _ -> adjustRatios(value.toInt(), "protein") }
        fatSlider.addOnChangeListener { _, value, _ -> adjustRatios(value.toInt(), "fat") }
        carbsSlider.addOnChangeListener { _, value, _ -> adjustRatios(value.toInt(), "carbs") }

        // Set up text watchers for weight inputs
        curWeightTextEdit.addTextChangedListener(weightTextWatcher)
        goalWeightTextEdit.addTextChangedListener(weightTextWatcher)

        // Calculate calories whenever the activity level is changed
        activityLevelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calculateRecommendedCalories()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }

    private val weightTextWatcher = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            calculateRecommendedCalories()
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    private fun calculateRecommendedCalories() {
        val curWeightText = curWeightTextEdit.text.toString()
        val goalWeightText = goalWeightTextEdit.text.toString()

        if (curWeightText.isNotEmpty() && goalWeightText.isNotEmpty()) {
            val currentWeight = curWeightText.toFloatOrNull()
            val goalWeight = goalWeightText.toFloatOrNull()

            if (currentWeight != null && goalWeight != null) {
                // Simple calorie adjustment logic
                val weightDifference = goalWeight - currentWeight
                val calorieAdjustment = (weightDifference * 500).roundToInt() // 500 calories per pound of weight change

                // Adjust base calories based on activity level
                val activityFactor = when (activityLevelSpinner.selectedItemPosition) {
                    0 -> 1.2f // Sedentary
                    1 -> 1.375f // Lightly Active
                    2 -> 1.55f // Moderately Active
                    3 -> 1.725f // Very Active
                    4 -> 1.9f // Extra Active
                    else -> 1.2f
                }

                val recommendedCalories = ((baseCalories + calorieAdjustment) * activityFactor).roundToInt()
                recommendedCaloriesTextEdit.text = "$recommendedCalories"
            }
        }
    }

    private fun adjustRatios(value: Int, sliderType: String) {
        if (isAdjusting) return // Prevent recursive updates
        isAdjusting = true

        // Get current values and convert them to integers
        var protein = proteinSlider.value.toInt()
        var fat = fatSlider.value.toInt()
        var carbs = carbsSlider.value.toInt()

        // Update the value of the changed slider
        when (sliderType) {
            "protein" -> protein = value
            "fat" -> fat = value
            "carbs" -> carbs = value
        }

        // Ensure the sum of all percentages is 100%
        val total = protein + fat + carbs
        if (total != 100) {
            val difference = 100 - total
            when (sliderType) {
                "protein" -> {
                    if (fat + difference >= 0) {
                        fat += difference
                    } else {
                        carbs += difference
                    }
                }
                "fat" -> {
                    if (carbs + difference >= 0) {
                        carbs += difference
                    } else {
                        protein += difference
                    }
                }
                "carbs" -> {
                    if (protein + difference >= 0) {
                        protein += difference
                    } else {
                        fat += difference
                    }
                }
            }
        }

        // Safely update slider values
        proteinSlider.value = protein.toFloat()
        fatSlider.value = fat.toFloat()
        carbsSlider.value = carbs.toFloat()

        // Update the TextViews
        updateTextViews()
        isAdjusting = false
    }

    private fun updateTextViews() {
        // Calculate grams based on calories
        val proteinCalories = (baseCalories * (proteinSlider.value / 100)).toInt()
        val fatCalories = (baseCalories * (fatSlider.value / 100)).toInt()
        val carbsCalories = (baseCalories * (carbsSlider.value / 100)).toInt()

        val proteinGrams = proteinCalories / 4 // 4 calories per gram of protein
        val fatGrams = fatCalories / 9 // 9 calories per gram of fat
        val carbsGrams = carbsCalories / 4 // 4 calories per gram of carbs

        // Update percentage TextViews
        proteinPTextView.text = "Protein: ${proteinSlider.value.toInt()}%"
        fatPTextView.text = "Fat: ${fatSlider.value.toInt()}%"
        carbsPTextView.text = "Carbs: ${carbsSlider.value.toInt()}%"

        // Update gram TextViews
        proteinGTextView.text = "Protein: $proteinGrams g"
        fatGTextView.text = "Fat: $fatGrams g"
        carbsGTextView.text = "Carbs: $carbsGrams g"
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the date and display it
                val formattedDate = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                goalDateButton.text = formattedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}
