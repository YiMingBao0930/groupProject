package com.cs407.grouplab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val metValue: Double, // MET value for calculating calories burned
    val description: String
)
