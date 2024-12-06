package com.cs407.grouplab

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import java.util.Date

@Entity(tableName = "user_goals")
data class UserGoal(
    @PrimaryKey val username: String,
    val currentWeight: Float,
    val goalWeight: Float,
    val targetDate: Date,
    val activityLevel: Int,
    val dailyCalories: Int,
    val proteinPercentage: Int,
    val fatPercentage: Int,
    val carbsPercentage: Int
)

@Dao
interface UserGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: UserGoal)

    @Query("SELECT * FROM user_goals WHERE username = :username")
    suspend fun getUserGoal(username: String): UserGoal?
}