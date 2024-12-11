package com.cs407.grouplab

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.cs407.grouplab.data.AppDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class RemindersFragment : Fragment() {

    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button in the toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "" // Hide the toolbar title text
        }

        // Handle back button click
        // Handle back button click to navigate to the home page
        toolbar.setNavigationOnClickListener {
            val homeFragment = AppHomePageFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, homeFragment)
                .commit()
        }

        // Check exact alarm permission for Android 12+
        checkExactAlarmPermission()
        loadSavedTimes()
        val breakfastCard = view.findViewById<MaterialCardView>(R.id.breakfast_card)
        val lunchCard = view.findViewById<MaterialCardView>(R.id.lunch_card)
        val dinnerCard = view.findViewById<MaterialCardView>(R.id.dinner_card)

        breakfastCard.setOnClickListener { showTimePicker("breakfast") }
        lunchCard.setOnClickListener { showTimePicker("lunch") }
        dinnerCard.setOnClickListener { showTimePicker("dinner") }

        // Load saved times

    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Exact Alarm Permission Required")
                    .setMessage("This app requires permission to schedule exact alarms for meal reminders. Please grant the permission.")
                    .setPositiveButton("Allow") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun showTimePicker(mealType: String) {
        val sharedPreferences = requireContext().getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val currentTime = sharedPreferences.getString("${mealType}_time", "12:00")?.split(":")
        val hour = currentTime?.get(0)?.toInt() ?: 12
        val minute = currentTime?.get(1)?.toInt() ?: 0

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTheme(R.style.CustomTimePickerTheme)
            .setTitleText("Set $mealType reminder")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            saveReminderTime(mealType, formattedTime)
            updateTimeDisplay(mealType, formattedTime)
            scheduleReminder(mealType, timePicker.hour, timePicker.minute)
        }

        timePicker.show(parentFragmentManager, null)
    }

    private fun saveReminderTime(mealType: String, time: String) {
        val sharedPreferences = requireContext().getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("${mealType}_time", time)
            apply()
        }
    }

    private fun loadSavedTimes() {
        val sharedPreferences = requireContext().getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

        val breakfastTime = sharedPreferences.getString("breakfast_time", "08:00")
        val lunchTime = sharedPreferences.getString("lunch_time", "13:00")
        val dinnerTime = sharedPreferences.getString("dinner_time", "19:00")

        Log.d("RemindersFragment", "Breakfast time: $breakfastTime")
        Log.d("RemindersFragment", "Lunch time: $lunchTime")
        Log.d("RemindersFragment", "Dinner time: $dinnerTime")

        updateTimeDisplay("breakfast", breakfastTime)
        updateTimeDisplay("lunch", lunchTime)
        updateTimeDisplay("dinner", dinnerTime)
    }


    private fun updateTimeDisplay(mealType: String, time: String?) {
        val timeText = view?.findViewById<TextView>(
            resources.getIdentifier("${mealType}_time", "id", requireContext().packageName)
        )
        timeText?.text = time ?: "--:--"
    }

    private fun scheduleReminder(mealType: String, hour: Int, minute: Int) {
        val intent = Intent(requireContext(), ReminderBroadcastReceiver::class.java).apply {
            action = "MEAL_REMINDER"
            putExtra("meal_type", mealType)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            getMealTypeCode(mealType),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
                Log.d("RemindersFragment", "Scheduled alarm for $mealType at $hour:$minute")
            } else {
                Toast.makeText(context, "Please allow exact alarms in settings", Toast.LENGTH_LONG).show()
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
            Log.d("RemindersFragment", "Scheduled alarm for $mealType at $hour:$minute")
        }
    }

    private fun getMealTypeCode(mealType: String): Int {
        return when (mealType) {
            "breakfast" -> 1
            "lunch" -> 2
            "dinner" -> 3
            else -> 0
        }
    }
}
