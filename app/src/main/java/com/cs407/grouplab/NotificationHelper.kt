package com.cs407.grouplab

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationHelper private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var instance: NotificationHelper? = null

        fun getInstance(context: Context): NotificationHelper =
            instance ?: synchronized(this) {
                instance ?: NotificationHelper(context).also { instance = it }
            }
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Notification channel details
    private val CHANNEL_ID = "meal_reminder_channel"
    private val CHANNEL_NAME = "Meal Reminders"
    private val CHANNEL_DESCRIPTION = "Notifications for meal reminders"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendMealReminder(mealType: String, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Time for $mealType!")
            .setContentText("It's $time - Don't forget to log your meal!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Enable sound, vibration, and lights
            .build()

        Log.d("NotificationHelper", "Sending notification for $mealType at $time")
        notificationManager.notify(mealType.hashCode(), notification)
    }
}
