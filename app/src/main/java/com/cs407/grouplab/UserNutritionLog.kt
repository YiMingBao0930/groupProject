package com.cs407.grouplab

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_nutrition_logs",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["foodItemId"])]
)
// Store food items consumed on given day, can then sum nutritional values of foods to get
// nutritional info
data class UserNutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val date: String, // Format: YYYY-MM-DD
    val foodItemId: Int, // Links to FoodItem
    val quantity: Float // Float to allow for partial servings
)
