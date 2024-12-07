package com.cs407.grouplab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_logs")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val date: String,
    val exerciseId: Long,
    val timeSpent: Int, // in minutes
    val caloriesBurned: Float
)
