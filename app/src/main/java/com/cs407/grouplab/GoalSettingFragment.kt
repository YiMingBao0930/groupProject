package com.cs407.grouplab

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.cs407.grouplab.data.AppDatabase
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import kotlin.math.roundToInt
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.cs407.grouplab.UserGoalDao


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
    private lateinit var confirmGoalButton: Button
    private lateinit var goalStepsEditText: TextView


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
        goalStepsEditText = view.findViewById(R.id.goalStepsTextEdit)

        goalDateButton = view.findViewById(R.id.goalDate_button)
        confirmGoalButton = view.findViewById((R.id.confirm_goal))

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
                recommendedCalories =
                    caloriesText.toIntOrNull() ?: 2000 // Default to 2000 if input is invalid
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
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                calculateRecommendedCalories()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set up the Confirm Goal button click listener
//        confirmGoalButton.setOnClickListener {
//            if (validateInputs()) {
//                // All inputs are valid, go to home page
//                val fragment = AppHomePageFragment()
//                parentFragmentManager.beginTransaction()
//                    .setCustomAnimations(
//                        R.anim.slide_in_right,
//                        R.anim.slide_out_left,
//                        R.anim.slide_in_right,
//                        R.anim.slide_out_left
//                    )
//                    .replace(R.id.fragment_container, fragment)
//                    .addToBackStack(null)
//                    .commit()
//
//            } else {
//                Snackbar.make(view, "One or more fields aren't filled out", Snackbar.LENGTH_SHORT)
//                    .show()
//            }
//        }
        confirmGoalButton.setOnClickListener {
            if (validateInputs()) {
                saveGoals()
            } else {
                Snackbar.make(view, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            }
        }

        loadUserGoals()

        return view
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Check if current weight is filled
        if (curWeightTextEdit.text.isEmpty()) {
            curWeightTextEdit.error = "Current weight is required"
            isValid = false
        } else {
            curWeightTextEdit.error = null
        }

        // Check if goal weight is filled
        if (goalWeightTextEdit.text.isEmpty()) {
            goalWeightTextEdit.error = "Goal weight is required"
            isValid = false
        } else {
            goalWeightTextEdit.error = null
        }

        // Check if goal steps is filled
        if (goalStepsEditText.text.isEmpty()) {
            goalStepsEditText.error = "Goal steps is required"
            isValid = false
        } else {
            goalStepsEditText.error = null
        }

        // Check if goal date is selected
        if (goalDateButton.text == "Goal Date") {
            goalDateButton.error = "Please select a goal date"
            isValid = false
        } else {
            goalDateButton.error = null
        }

        // Check if activity level is selected
        if (activityLevelSpinner.selectedItemPosition == AdapterView.INVALID_POSITION) {
            isValid = false
        }

        return isValid
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
                    // Calculate BMR (using Mifflin-St Jeor equation)
                    val weightKg = currentWeight / 2.205 // Convert lbs to kg
                    val bmr = (10 * weightKg + 6.25 * 170 - 5 * 30 + 5).toInt() // Assuming height=170cm, age=30, male
                    baseCalories = bmr

                    // Parse the goal date
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val goalDate = sdf.parse(goalDateText)
                    val today = Calendar.getInstance().time

                    // Calculate days until goal date
                    val daysUntilGoal = ((goalDate!!.time - today.time) / (1000 * 60 * 60 * 24)).toInt()

                    // Calculate daily calorie adjustment based on weight goal
                    val weightDifference = goalWeight - currentWeight
                    val totalCalorieChange = (weightDifference * 3500).toInt() // 3500 calories per pound
                    val dailyCalorieChange = if (daysUntilGoal > 0) {
                        (totalCalorieChange / daysUntilGoal).coerceIn(-1000, 1000) // Limit daily adjustment
                    } else {
                        0 // No adjustment if goal date is today
                    }

                    // Adjust base calories based on activity level
                    val activityFactor = when (activityLevelSpinner.selectedItemPosition) {
                        0 -> 1.2f    // Sedentary (little or no exercise)
                        1 -> 1.375f  // Lightly Active (exercise 1-3 days/week)
                        2 -> 1.55f   // Moderately Active (exercise 3-5 days/week)
                        3 -> 1.725f  // Very Active (exercise 6-7 days/week)
                        else -> 1.2f // Default to sedentary if something goes wrong
                    }

                    // Calculate final recommended calories
                    recommendedCalories = ((baseCalories * activityFactor) + dailyCalorieChange).roundToInt()
                    
                    // Ensure minimum safe calories (1200 for women, 1500 for men)
                    val minimumCalories = 1500 // Using male minimum as default
                    recommendedCalories = recommendedCalories.coerceAtLeast(minimumCalories)
                    
                    // Update UI
                    recommendedCaloriesTextEdit.setText(recommendedCalories.toString())

                    // Update macronutrient ratios based on new calorie goal
                    setDefaultMacroRatios()
                    updateTextViews()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Show error message to user
                    Snackbar.make(
                        requireView(),
                        "Error calculating calories. Please check your inputs.",
                        Snackbar.LENGTH_SHORT
                    ).show()
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

    private fun saveGoals() {
        val sharedPreferences =
            requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null) ?: return

        // Get values from inputs
        val currentWeight = curWeightTextEdit.text.toString().toFloatOrNull() ?: return
        val goalWeight = goalWeightTextEdit.text.toString().toFloatOrNull() ?: return
        val targetDate = try {
            val inputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = inputDateFormat.parse(goalDateButton.text.toString())
            outputDateFormat.format(parsedDate) // Format the parsed date to yyyy-MM-dd
        } catch (e: Exception) {
            null
        } ?: return

        val activityLevel = activityLevelSpinner.selectedItemPosition
        val calories = recommendedCaloriesTextEdit.text.toString().toIntOrNull() ?: return
        val protein = proteinSlider.value.toInt()
        val fat = fatSlider.value.toInt()
        val carbs = carbsSlider.value.toInt()
        val stepsGoal = goalStepsEditText.text.toString().toIntOrNull() ?: return
        val proteinGrams = proteinGTextView.text.toString()
            .replace("Protein:", "")
            .replace("g", "")
            .trim()
            .toIntOrNull() ?: 0

        val carbGrams = carbsGTextView.text.toString()
            .replace("Carbs:", "")
            .replace("g", "")
            .trim()
            .toIntOrNull() ?: 0

        val fatGrams = fatGTextView.text.toString()
            .replace("Fat:", "")
            .replace("g", "")
            .trim()
            .toIntOrNull() ?: 0


        val goal = UserGoal(
            username = username,
            currentWeight = currentWeight,
            goalWeight = goalWeight,
            targetDate = targetDate,
            activityLevel = activityLevel,
            dailyCalories = calories,
            proteinPercentage = protein,
            fatPercentage = fat,
            carbsPercentage = carbs,
            proteinGram = proteinGrams,
            carbGram = carbGrams,
            fatGram = fatGrams,
            stepsGoal = stepsGoal
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                AppDatabase.getDatabase(requireContext()).userGoalDao().insert(goal)
                populateFoodDatabase(AppDatabase.getDatabase(requireContext()).foodItemDao())
                withContext(Dispatchers.Main) {
                    // Navigate to home page on success
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(requireView(), "Error saving goals", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserGoals() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null) ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userGoal = AppDatabase.getDatabase(requireContext()).userGoalDao().getUserGoal(username)
                
                withContext(Dispatchers.Main) {
                    userGoal?.let { goal ->
                        // Update weight fields
                        curWeightTextEdit.setText(goal.currentWeight.toString())
                        goalWeightTextEdit.setText(goal.goalWeight.toString())
                        
                        // Update goal date
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val displayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        val date = dateFormat.parse(goal.targetDate)
                        date?.let {
                            goalDateButton.text = displayFormat.format(it)
                        }
                        
                        // Update activity level
                        activityLevelSpinner.setSelection(goal.activityLevel)
                        
                        // Update calories
                        recommendedCaloriesTextEdit.setText(goal.dailyCalories.toString())
                        
                        // Update macro sliders
                        proteinSlider.value = goal.proteinPercentage.toFloat()
                        fatSlider.value = goal.fatPercentage.toFloat()
                        carbsSlider.value = goal.carbsPercentage.toFloat()
                        
                        // Update steps goal
                        goalStepsEditText.setText(goal.stepsGoal.toString())
                        
                        // Update macro text views
                        updateTextViews()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(requireView(), "Error loading goals", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }



}
