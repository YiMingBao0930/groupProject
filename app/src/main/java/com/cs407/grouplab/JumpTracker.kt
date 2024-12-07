package com.cs407.grouplab

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JumpTracker : AppCompatActivity() {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var jumpCount = 0
    private var isJumping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_page)

        // Initialize sensor services
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Start jump tracking
        startJumpTracking()
    }

    private fun startJumpTracking() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show()
            return
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val zAcceleration = event.values[2] // Z-axis acceleration
            if (zAcceleration > 15 && !isJumping) {
                isJumping = true
                jumpCount++
                updateJumpUI()
            } else if (zAcceleration < 5) {
                isJumping = false
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun updateJumpUI() {
        findViewById<TextView>(R.id.jump_text).text = "Jumps: $jumpCount"
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onResume() {
        super.onResume()
        startJumpTracking()
    }
}
