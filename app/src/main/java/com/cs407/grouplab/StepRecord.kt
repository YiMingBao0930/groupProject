package com.cs407.grouplab

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // User-specific step data
    val date: String,       // Track steps by day
    val initialStepCount: Float, // Offset for the day's step count
    val steps: Int,       // Total steps for the user on the day
    val lastTotalSteps: Float    // Last total steps recorded by the sensor
)


