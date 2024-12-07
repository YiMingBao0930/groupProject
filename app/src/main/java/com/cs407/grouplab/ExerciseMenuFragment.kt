package com.cs407.grouplab

import ExerciseAdapter
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseMenuFragment : Fragment(), SensorEventListener {

    private lateinit var stepsTextView: TextView
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var initialStepCount: Float? = null
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var loggedInUser: String
    private lateinit var stepRecordDao: StepRecordDao
    private lateinit var caloriesBurnedTextView: TextView
    private var today: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private var global_steps = 0f
    private lateinit var caloriesRecordDao: CaloriesRecordDao
    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var totalCaloriesBurnedTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        stepRecordDao = db.stepRecordDao()
        caloriesRecordDao = db.caloriesRecordDao()


        // Check and request permissions for activity recognition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                    1
                )
            }
        }

        // Initialize SharedPreferences
        sharedPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        loggedInUser = sharedPrefs.getString("logged_in_username", null).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.exercise_menu, container, false)

        caloriesBurnedTextView = view.findViewById(R.id.step_calorie_burned)
        populateExercisesIfNeeded()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the stepsTextView
        stepsTextView = view.findViewById(R.id.steps_text_view)
        totalCaloriesBurnedTextView = view.findViewById(R.id.total_calories_burned)


        // Reset `initialStepCount` when a new user logs in
        resetStepTrackingForNewUser()


        exerciseRecyclerView = view.findViewById(R.id.exercise_recycler_view)
        exerciseAdapter = ExerciseAdapter { exercise ->
            // Handle exercise item click, e.g., show a dialog for time spent
            val dialog = LogExerciseDialogFragment(exercise)
            dialog.show(parentFragmentManager, "ExerciseLogDialog")
        }

        exerciseRecyclerView.adapter = exerciseAdapter
        exerciseRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val exercises = db.exerciseDao().getAllExercises()
            exerciseAdapter.submitList(exercises)
        }


        // Initialize SensorManager and Step Counter Sensor
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(requireContext(), "Step Counter Sensor not available!", Toast.LENGTH_LONG).show()
        }

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
        lifecycleScope.launch {
            val userGoal = fetchUserGoal()
            val goalSteps = userGoal?.stepsGoal ?: 0
            val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Convert kg
            val caloriesPerStep = calculateCaloriesPerStep(weightKg)
            //updateStepsView(0, goalSteps, caloriesPerStep) // Set initial steps to 0
        }

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (!username.isNullOrEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(requireContext())

                // Retrieve the LiveData objects for exercise and step calories
                val exerciseCaloriesLive = db.exerciseLogDao().getTotalCaloriesBurned(username, currentDate)
                val stepCaloriesLive = db.caloriesRecordDao().getCaloriesRecordForDate(username, currentDate)

                withContext(Dispatchers.Main) {
                    // Observe LiveData for exercise calories
                    exerciseCaloriesLive.observe(viewLifecycleOwner) { exerciseCalories ->
                        // Observe LiveData for step calories
                        stepCaloriesLive.observe(viewLifecycleOwner) { stepCaloriesRecord ->
                            val exerciseCaloriesValue = exerciseCalories ?: 0f
                            val stepCaloriesValue = stepCaloriesRecord?.caloriesBurned ?: 0f

                            val totalCalories = exerciseCaloriesValue + stepCaloriesValue

                            // Update the total calories burned text view
                            totalCaloriesBurnedTextView.text = getString(
                                R.string.total_calories_burned,
                                totalCalories.toInt()
                            )
                        }
                    }
                }
            }
        }


    }

    private suspend fun fetchUserGoal(): UserGoal? {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            db.userGoalDao().getUserGoal(loggedInUser)
        }
    }


    override fun onResume() {
        super.onResume()
        // Register the listener for the Step Counter sensor
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the sensor listener to save battery
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0]
            global_steps = event.values[0]
            if (initialStepCount == null) {
                // Set the initial step count value
                initialStepCount = steps
            }
            // Calculate the steps taken since the initial count
            val currentSteps = steps - initialStepCount!!

            lifecycleScope.launch {
                saveSteps(currentSteps.toInt(), steps)
            }
            lifecycleScope.launch {
                val userGoal = fetchUserGoal()
                val goalSteps = userGoal?.stepsGoal ?: 0
                val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Default to 70kg
                val caloriesPerStep = calculateCaloriesPerStep(weightKg)

                saveSteps(currentSteps.toInt(), steps)
                //updateStepsView(currentSteps.toInt(), goalSteps, caloriesPerStep)
            }
        }
    }

    // uses formula based on weight and avg step time to get calories per step
    private fun calculateCaloriesPerStep(weightKg: Double): Double {
        val met = 3.8
        val stepTimeInHours = 0.5 / 60.0 // minutes actually
        return 3.5 * met * weightKg * stepTimeInHours / 200
    }

    private suspend fun saveSteps(stepsSinceLast: Int, currentSensorValue: Float) {
        Log.d("ExerciseFragment", "Saving steps. Steps Since Last: $stepsSinceLast, Sensor Value: $currentSensorValue")

        // Get the record with the highest `lastTotalSteps`
        val highestRecord = stepRecordDao.getStepRecordWithMaxLastTotalSteps()
        val maxLastTotalSteps = highestRecord?.lastTotalSteps ?: 0f
        Log.d("ExerciseFragment", "Highest lastTotalSteps across all users: $maxLastTotalSteps")

        // Fetch the current user's step record for today
        val stepRecord = stepRecordDao.getStepRecordForDate(loggedInUser, today)
        val caloriesRecord = caloriesRecordDao.getCaloriesRecordForDate(loggedInUser, today)

        // make sure first created step Record does not have sensor total steps
        if (highestRecord == null) {
            // do nothing maybe???
        }
        else if (stepRecord != null) {
            if (stepRecord == highestRecord) {
                // Update the current user's record normally if it matches the highest record
                val newSteps = (currentSensorValue - stepRecord.lastTotalSteps).toInt()
                if (newSteps > 0) {
                    val updatedSteps = stepRecord.steps + newSteps
                    Log.d("ExerciseFragment", "Updated Steps: $updatedSteps, Last Total Steps: $currentSensorValue")

                    // Update the database for the current user
                    stepRecordDao.updateStepsAndLastTotal(
                        stepRecord.id,
                        updatedSteps,
                        currentSensorValue
                    )
                    val userGoal = fetchUserGoal()
                    val goalSteps = userGoal?.stepsGoal ?: 0
                    val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Default to 70kg
                    val caloriesPerStep = calculateCaloriesPerStep(weightKg)
                    caloriesRecordDao.updateCaloriesForDate(loggedInUser, today, (updatedSteps * caloriesPerStep).toFloat())
                    updateStepsView(updatedSteps, goalSteps, caloriesPerStep)
                } else {
                    Log.d("ExerciseFragment", "No new steps to save for current user.")
                }
            } else {
                if (highestRecord != null) {
                    // Handle case where the user's record is not the highest
                    Log.d(
                        "ExerciseFragment",
                        "Current user's record is not the highest lastTotalSteps."
                    )
                    val correctedSteps = (currentSensorValue - maxLastTotalSteps).toInt()
                    if (correctedSteps > 0) {
                        val updatedSteps = highestRecord.steps + correctedSteps
                        Log.d(
                            "ExerciseFragment",
                            "Corrected Steps: $updatedSteps, Last Total Steps: $currentSensorValue"
                        )

                        // Update the highest user's record
                        stepRecordDao.updateStepsAndLastTotal(
                            highestRecord.id,
                            updatedSteps,
                            highestRecord.lastTotalSteps
                        )
                        val userGoal = fetchUserGoal()
                        val goalSteps = userGoal?.stepsGoal ?: 0
                        val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Default to 70kg
                        val caloriesPerStep = calculateCaloriesPerStep(weightKg)
                        caloriesRecordDao.updateCaloriesForDate(highestRecord.username, highestRecord.date, (updatedSteps * caloriesPerStep).toFloat())

                        stepRecordDao.updateStepsAndLastTotal(
                            stepRecord.id,
                            stepRecord.steps,
                            currentSensorValue
                        )
                    }
                }
            }
        } else if (highestRecord != null && highestRecord.username != loggedInUser) {
            // If no record for the current user exists, but another user's record is the highest
            Log.d("ExerciseFragment", "No record for current user; adjusting steps from highest record.")
            val correctedSteps = (currentSensorValue - maxLastTotalSteps).toInt()
            if (correctedSteps > 0) {
                // Create a new record for the current user
                /*
                val newRecord = StepRecord(
                    username = loggedInUser,
                    date = today,
                    initialStepCount = maxLastTotalSteps,
                    steps = correctedSteps,
                    lastTotalSteps = currentSensorValue
                )
                stepRecordDao.insertStepRecord(newRecord)
                */
            }
        } else {
            // If no record exists for the current user and no highest record, create a new record
            Log.d("ExerciseFragment", "No record found for today. Creating new record for current user.")
            /*
            val newRecord = StepRecord(
                username = loggedInUser,
                date = today,
                initialStepCount = currentSensorValue,
                steps = stepsSinceLast,
                lastTotalSteps = currentSensorValue
            )
            stepRecordDao.insertStepRecord(newRecord)

             */
        }
    }




    private fun resetStepTrackingForNewUser() {
        lifecycleScope.launch {
            // Fetch today's record for the new user
            val stepRecord = stepRecordDao.getStepRecordForDate(loggedInUser, today)
            if (stepRecord != null) {
                initialStepCount = stepRecord.lastTotalSteps
                Log.d("ExerciseFragment", "Loaded step record for user: $loggedInUser")
                val userGoal = fetchUserGoal()
                val goalSteps = userGoal?.stepsGoal ?: 0
                val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Default to 70kg
                val caloriesPerStep = calculateCaloriesPerStep(weightKg)
                updateStepsView(stepRecord.steps, goalSteps, caloriesPerStep)
            } else {
                initialStepCount = null
                Log.d("ExerciseFragment", "No record found for user: $loggedInUser")
                val userGoal = fetchUserGoal()
                val goalSteps = userGoal?.stepsGoal ?: 0
                val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Default to 70kg
                val caloriesPerStep = calculateCaloriesPerStep(weightKg)
                updateStepsView(0, goalSteps, caloriesPerStep)
                val dummyRecordName = "DUMMY STEP RECORD DO NOT LOG IN AS"
                if (stepRecordDao.getEarliestStepRecordForUser(dummyRecordName) == null) {
                    val dummyRecord = StepRecord(
                        username = dummyRecordName,
                        date = today,
                        initialStepCount = 0f,
                        steps = 0,
                        lastTotalSteps = 0f
                    )
                    stepRecordDao.insertStepRecord(dummyRecord)
                }
                val newCaloriesRecord = CaloriesRecord(
                    username = loggedInUser,
                    date = today,
                    caloriesBurned = 0f
                )
                caloriesRecordDao.insertCaloriesRecord(newCaloriesRecord)
                val newRecord = StepRecord(
                    username = loggedInUser,
                    date = today,
                    initialStepCount = global_steps,
                    steps = 0,
                    lastTotalSteps = global_steps
                )
                stepRecordDao.insertStepRecord(newRecord)
            }
        }
    }

    private fun populateExercisesIfNeeded() {
        val exerciseList = listOf(
            // High-Intensity Exercises
            Exercise(name = "Running", metValue = 9.8, description = "High-intensity cardio exercise."),
            Exercise(name = "Cycling (16-19 mph)", metValue = 12.0, description = "High-speed outdoor or indoor cycling."),
            Exercise(name = "Jump Rope", metValue = 11.0, description = "Cardio using a jump rope."),
            Exercise(name = "Swimming (freestyle)", metValue = 7.0, description = "Continuous freestyle swimming."),
            Exercise(name = "Rowing (vigorous)", metValue = 7.5, description = "Rowing machine workout at high effort."),
            Exercise(name = "Stair Climbing", metValue = 8.8, description = "Vigorous stair climbing."),

            // Moderate-Intensity Exercises
            Exercise(name = "Walking (4 mph)", metValue = 5.0, description = "Brisk walking pace."),
            Exercise(name = "Dancing", metValue = 6.0, description = "High-energy dance routines."),
            Exercise(name = "Cycling (10-12 mph)", metValue = 6.8, description = "Moderate-speed outdoor or indoor cycling."),
            Exercise(name = "Hiking", metValue = 6.0, description = "Walking on uneven terrain."),
            Exercise(name = "Aerobics", metValue = 6.0, description = "Group or solo aerobic exercises."),
            Exercise(name = "Elliptical Trainer", metValue = 5.0, description = "Moderate-intensity elliptical workout."),

            // Sports and Recreational Activities
            Exercise(name = "Basketball (game)", metValue = 8.0, description = "Full-court basketball."),
            Exercise(name = "Soccer (game)", metValue = 10.0, description = "Competitive soccer game."),
            Exercise(name = "Tennis (singles)", metValue = 7.0, description = "Singles tennis match."),
            Exercise(name = "Volleyball (competitive)", metValue = 6.0, description = "Competitive volleyball match."),
            Exercise(name = "Badminton", metValue = 4.5, description = "Recreational badminton game."),
            Exercise(name = "Table Tennis", metValue = 4.0, description = "Recreational table tennis match."),
            Exercise(name = "Golf (carrying clubs)", metValue = 4.3, description = "Walking and carrying clubs."),
            Exercise(name = "Baseball", metValue = 5.0, description = "Playing baseball or softball.")
        )

        val exerciseNames = exerciseList.map { it.name }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val existingNames = db.exerciseDao().getExistingExerciseNames(exerciseNames)

            val newExercises = exerciseList.filterNot { it.name in existingNames }

            if (newExercises.isNotEmpty()) {
                db.exerciseDao().insertExercises(newExercises)
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed, but must override
    }

    private fun updateStepsView(currentSteps: Int, goalSteps: Int, caloriesPerStep: Double) {
        activity?.runOnUiThread {
            stepsTextView.text = getString(R.string.step_goal, currentSteps, goalSteps)

            val caloriesBurned = (currentSteps * caloriesPerStep).toInt()
            caloriesBurnedTextView.text = getString(R.string.steps_calories_burned, caloriesBurned)
        }
    }
}

