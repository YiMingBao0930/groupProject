package com.cs407.grouplab

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "calories_records")
data class CaloriesRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val date: String,
    val caloriesBurned: Float
)

@Dao
interface CaloriesRecordDao {

    @Insert
    suspend fun insertCaloriesRecord(record: CaloriesRecord)

    @Query("SELECT * FROM calories_records WHERE username = :username AND date = :date LIMIT 1")
    fun getCaloriesRecordForDate(username: String, date: String): LiveData<CaloriesRecord?>

    @Query("UPDATE calories_records SET caloriesBurned = :updatedCalories WHERE username = :username AND date = :date")
    suspend fun updateCaloriesForDate(username: String, date: String, updatedCalories: Float)
}
