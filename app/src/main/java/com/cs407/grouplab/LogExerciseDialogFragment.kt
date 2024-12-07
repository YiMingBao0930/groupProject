package com.cs407.grouplab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.cs407.grouplab.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogExerciseDialogFragment(private val exercise: Exercise) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_log_exercise, container, false)
        val nameText: TextView = view.findViewById(R.id.exercise_name)
        val timeSpentInput: EditText = view.findViewById(R.id.time_spent_input)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)

        nameText.text = exercise.name

        confirmButton.setOnClickListener {
            val timeSpent = timeSpentInput.text.toString().toIntOrNull()
            if (timeSpent != null && timeSpent > 0) {
                logExercise(timeSpent)
                dismiss()
            }
        }
        return view
    }

    private fun logExercise(timeSpent: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("logged_in_username", null)
        val db = AppDatabase.getDatabase(requireContext())
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (username != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                // Fetch user weight
                val userGoal = db.userGoalDao().getUserGoal(username)
                val weightKg = userGoal?.currentWeight?.let { it / 2.205 } ?: 70.0 // Default to 70 kg if not set

                // Calculate calories burned
                val caloriesBurned = (3.5 * exercise.metValue * weightKg * timeSpent / 200).toFloat()

                // Create and insert log
                val log = ExerciseLog(
                    username = username,
                    date = date,
                    exerciseId = exercise.id,
                    timeSpent = timeSpent,
                    caloriesBurned = caloriesBurned
                )
                db.exerciseLogDao().insertExerciseLog(log)
            }
        }
    }

}
