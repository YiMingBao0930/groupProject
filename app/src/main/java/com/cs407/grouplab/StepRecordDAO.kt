package com.cs407.grouplab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StepRecordDao {
    @Insert
    suspend fun insertStepRecord(record: StepRecord)

    @Query("SELECT * FROM step_records WHERE username = :username AND date = :date LIMIT 1")
    suspend fun getStepRecordForDate(username: String, date: String): StepRecord?

    @Query("SELECT * FROM step_records WHERE username = :username ORDER BY date ASC LIMIT 1")
    suspend fun getEarliestStepRecordForUser(username: String): StepRecord?


    @Query("UPDATE step_records SET steps = :additionalSteps, lastTotalSteps = :currentTotalSteps WHERE id = :id")
    suspend fun updateStepsAndLastTotal(id: Long, additionalSteps: Int, currentTotalSteps: Float)

    @Query("SELECT MAX(lastTotalSteps) FROM step_records")
    suspend fun getMaxLastTotalSteps(): Float?

    @Query("SELECT * FROM step_records WHERE lastTotalSteps = (SELECT MAX(lastTotalSteps) FROM step_records)")
    suspend fun getStepRecordWithMaxLastTotalSteps(): StepRecord?

}

