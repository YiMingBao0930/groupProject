package com.cs407.grouplab

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.grouplab.data.AppDatabase
import kotlinx.coroutines.launch
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
    private var today: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        stepRecordDao = db.stepRecordDao()

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

        // Reset `initialStepCount` when a new user logs in
        resetStepTrackingForNewUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.exercise_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the stepsTextView
        stepsTextView = view.findViewById(R.id.steps_text_view)

        // Initialize SensorManager and Step Counter Sensor
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(requireContext(), "Step Counter Sensor not available!", Toast.LENGTH_LONG).show()
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
            if (initialStepCount == null) {
                // Set the initial step count value
                initialStepCount = steps
            }
            // Calculate the steps taken since the initial count
            val currentSteps = steps - initialStepCount!!

            lifecycleScope.launch {
                saveSteps(currentSteps.toInt(), steps)
            }
            updateStepsView(currentSteps.toInt())
        }
    }

    private suspend fun saveSteps(stepsSinceLast: Int, currentSensorValue: Float) {
        Log.d("ExerciseFragment", "Saving steps. Steps Since Last: $stepsSinceLast, Sensor Value: $currentSensorValue")

        // Get the highest `lastTotalSteps` from all records
        val maxLastTotalSteps = stepRecordDao.getMaxLastTotalSteps() ?: 0f
        Log.d("ExerciseFragment", "Highest lastTotalSteps across all users: $maxLastTotalSteps")

        val stepRecord = stepRecordDao.getStepRecordForDate(loggedInUser, today)

        if (stepRecord != null) {
            // Calculate steps to add based on the difference from the highest `lastTotalSteps`
            val newSteps = (currentSensorValue - maxLastTotalSteps).toInt()
            if (newSteps > 0) {
                val updatedSteps = stepRecord.steps + newSteps
                Log.d("ExerciseFragment", "Updated Steps: $updatedSteps")

                // Update the database with the new steps and sensor value
                stepRecordDao.updateStepsAndLastTotal(
                    stepRecord.id,
                    updatedSteps,
                    currentSensorValue
                )
                updateStepsView(updatedSteps)
            } else {
                Log.d("ExerciseFragment", "No new steps to save.")
            }
        } else {
            // Create a new record if none exists
            Log.d("ExerciseFragment", "No record found for today. Creating new record.")
            val newRecord = StepRecord(
                username = loggedInUser,
                date = today,
                initialStepCount = currentSensorValue,
                steps = stepsSinceLast,
                lastTotalSteps = currentSensorValue
            )
            stepRecordDao.insertStepRecord(newRecord)
            updateStepsView(stepsSinceLast)
        }
    }


    private fun resetStepTrackingForNewUser() {
        lifecycleScope.launch {
            // Fetch today's record for the new user
            val stepRecord = stepRecordDao.getStepRecordForDate(loggedInUser, today)
            if (stepRecord != null) {
                // Initialize `initialStepCount` to `lastTotalSteps` for the new user
                initialStepCount = stepRecord.lastTotalSteps
                Log.d("ExerciseFragment", "Loaded step record for user: $loggedInUser")
                updateStepsView(stepRecord.steps)
            } else {
                // Reset to 0 if no record exists
                initialStepCount = null
                Log.d("ExerciseFragment", "No record found for user: $loggedInUser")
                updateStepsView(0)
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed, but must override
    }

    private fun updateStepsView(steps: Int) {
        // Safely update the stepsTextView
        activity?.runOnUiThread {
            stepsTextView.text = "Steps Today: $steps"
        }
    }
}

