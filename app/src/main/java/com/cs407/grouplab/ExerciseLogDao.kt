package com.cs407.grouplab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseLogDao {
    @Insert
    suspend fun insertExerciseLog(log: ExerciseLog)

    @Query("SELECT * FROM exercise_logs WHERE username = :username AND date = :date")
    suspend fun getLogsForUserAndDate(username: String, date: String): List<ExerciseLog>

    @Query("SELECT SUM(caloriesBurned) FROM exercise_logs WHERE username = :username AND date = :date")
    suspend fun getTotalCaloriesBurned(username: String, date: String): Float?
}
