package com.cs407.grouplab

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.Slider
import java.text.SimpleDateFormat
import kotlin.math.roundToInt
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


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
    private var recommendedCalories = 2000 // Initialize recommendedCalories
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

        // Set up TextWatcher for recommendedCaloriesTextEdit
        recommendedCaloriesTextEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update recommendedCalories and recalculate values
                val caloriesText = s.toString()
                recommendedCalories = caloriesText.toIntOrNull() ?: 2000 // Default to 2000 if input is invalid
                updateTextViews() // Recalculate values
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
        curWeightTextEdit.addTextChangedListener(weightTextWatcher)
        goalWeightTextEdit.addTextChangedListener(weightTextWatcher)
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
        val goalDateText = goalDateButton.text.toString()

        // Check if all required fields are filled and the goal date is valid
        if (curWeightText.isNotEmpty() && goalWeightText.isNotEmpty() && goalDateText != "Goal Date") {
            val currentWeight = curWeightText.toFloatOrNull()
            val goalWeight = goalWeightText.toFloatOrNull()

            if (currentWeight != null && goalWeight != null) {
                try {
                    // Parse the goal date
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val goalDate = sdf.parse(goalDateText)
                    val today = Calendar.getInstance().time

                    // Calculate days until goal date
                    val daysUntilGoal = ((goalDate!!.time - today.time) / (1000 * 60 * 60 * 24)).toInt()

                    // Calculate daily calorie adjustment
                    val weightDifference = goalWeight - currentWeight
                    val totalCalorieChange = (weightDifference * 3500).toInt() // 3500 calories per pound
                    val dailyCalorieChange = (totalCalorieChange / daysUntilGoal).coerceIn(-1000, 1000)

                    // Adjust base calories based on activity level
                    val activityFactor = when (activityLevelSpinner.selectedItemPosition) {
                        0 -> 1.2f // Sedentary
                        1 -> 1.375f // Lightly Active
                        2 -> 1.55f // Moderately Active
                        3 -> 1.725f // Very Active
                        else -> 1.2f
                    }

                    recommendedCalories = ((baseCalories + dailyCalorieChange) * activityFactor).roundToInt()
                    recommendedCaloriesTextEdit.setText("$recommendedCalories")

                    // Update macronutrient ratios
                    setDefaultMacroRatios()
                    updateTextViews()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle any parsing errors
                }
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
        // Calculate grams based on recommendedCalories
        val proteinCalories = (recommendedCalories * (proteinSlider.value / 100)).toInt()
        val fatCalories = (recommendedCalories * (fatSlider.value / 100)).toInt()
        val carbsCalories = (recommendedCalories * (carbsSlider.value / 100)).toInt()

        // Convert calories to grams
        val proteinGrams = proteinCalories / 4 // 4 calories per gram of protein
        val fatGrams = fatCalories / 9 // 9 calories per gram of fat
        val carbsGrams = carbsCalories / 4 // 4 calories per gram of carbs

        // Update percentage TextViews
        proteinPTextView.text = "Protein: ${proteinSlider.value.toInt()}%"
        fatPTextView.text = "Fat: ${fatSlider.value.toInt()}%"
        carbsPTextView.text = "Carbs: ${carbsSlider.value.toInt()}%"

        // Update gram TextViews with the correct values
        proteinGTextView.text = "Protein: $proteinGrams g"
        fatGTextView.text = "Fat: $fatGrams g"
        carbsGTextView.text = "Carbs: $carbsGrams g"
    }


    private fun setDefaultMacroRatios() {
        proteinSlider.value = 30f // 30% Protein
        fatSlider.value = 25f // 25% Fat
        carbsSlider.value = 45f // 45% Carbs
        updateTextViews()
    }

    private fun showDatePickerDialog() {
        // Get the current time in milliseconds
        val calendar = Calendar.getInstance()
        val todayInMillis = calendar.timeInMillis

        // Set up constraints to allow only future dates
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(todayInMillis) // Start from today
            .setValidator(DateValidatorFromToday()) // Use the custom DateValidator

        // Create the MaterialDatePicker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setTitleText("Select Goal Date")
            .setTheme(R.style.CustomMaterialDatePicker) // Apply the custom theme
            .build()

        // Show the date picker
        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")

        // Handle the date selection
        datePicker.addOnPositiveButtonClickListener { selection ->
            // Use Calendar to properly adjust for the timezone
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection

            // Format the adjusted calendar date
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)

            goalDateButton.text = formattedDate
        }
    }

}
