package com.cs407.grouplab

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MEAL_REMINDER") {
            val mealType = intent.getStringExtra("meal_type") ?: return
            val hour = intent.getIntExtra("hour", 0)
            val minute = intent.getIntExtra("minute", 0)

            // Add debug logging
            Log.d("ReminderReceiver", "Received reminder for $mealType at $hour:$minute")

            val time = String.format("%02d:%02d", hour, minute)
            NotificationHelper.getInstance(context).sendMealReminder(mealType, time)
        }
    }
}
