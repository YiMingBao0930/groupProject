package com.cs407.grouplab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Int, // kCal
    val protein: Int, // g
    val carbs: Int, // g
    val fat: Int, // g
    val saturatedFat: Int, // g
    val unsaturatedFat: Int, // g
    val cholesterol: Int, // mg
    val sodium: Int, // mg
    val potassium: Int, // mg
    val fiber: Int, // g
    val sugar: Int, // g
    val vitaminA: Int, // mg
    val vitaminB: Int, // mg
    val vitaminC: Int, // mg
    val vitaminD: Int, // mg
    val calcium: Int, // mg
    val iron: Int // mg
)
