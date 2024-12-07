package com.cs407.grouplab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("""
        SELECT name 
        FROM exercises 
        WHERE name IN (:exerciseNames)
    """)
    suspend fun getExistingExerciseNames(exerciseNames: List<String>): List<String>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    // Calculate calories burned for a specific exercise, given the user's weight and duration
    @Query("""
        SELECT (metValue * :weightKg * :durationMinutes / 60) AS caloriesBurned 
        FROM exercises 
        WHERE id = :exerciseId
    """)
    suspend fun calculateCaloriesBurned(exerciseId: Long, weightKg: Double, durationMinutes: Int): Double
}
