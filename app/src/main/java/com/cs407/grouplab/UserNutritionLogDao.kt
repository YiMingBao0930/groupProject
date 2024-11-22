package com.cs407.grouplab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserNutritionLogDao {
    @Insert
    suspend fun insert(log: UserNutritionLog)

    @Query("""
        SELECT 
            SUM(f.calories * u.quantity) AS totalCalories,
            SUM(f.protein * u.quantity) AS totalProtein,
            SUM(f.carbs * u.quantity) AS totalCarbs,
            SUM(f.fat * u.quantity) AS totalFat,
            SUM(f.saturatedFat * u.quantity) AS totalSaturatedFat,
            SUM(f.unsaturatedFat * u.quantity) AS totalUnsaturatedFat,
            SUM(f.cholesterol * u.quantity) AS totalCholesterol,
            SUM(f.sodium * u.quantity) AS totalSodium,
            SUM(f.potassium * u.quantity) AS totalPotassium,
            SUM(f.fiber * u.quantity) AS totalFiber,
            SUM(f.sugar * u.quantity) AS totalSugar,
            SUM(f.vitaminA * u.quantity) AS totalVitaminA,
            SUM(f.vitaminB * u.quantity) AS totalVitaminB,
            SUM(f.vitaminC * u.quantity) AS totalVitaminC,
            SUM(f.vitaminD * u.quantity) AS totalVitaminD,
            SUM(f.calcium * u.quantity) AS totalCalcium,
            SUM(f.iron * u.quantity) AS totalIron
        FROM user_nutrition_logs u
        INNER JOIN food_items f ON u.foodItemId = f.id
        WHERE u.username = :username AND u.date = :date
    """)
    suspend fun getDailyNutrition(username: String, date: String): DailyNutritionSummary

    @Query("SELECT * FROM user_nutrition_logs WHERE username = :username AND date >= :startDate ORDER BY date DESC")
    suspend fun getLogsForLast30Days(username: String, startDate: String): List<UserNutritionLog>

    @Query("DELETE FROM user_nutrition_logs WHERE date < :thresholdDate")
    suspend fun deleteLogsOlderThan(thresholdDate: String)
}
