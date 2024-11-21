package com.cs407.grouplab

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "food_items")
data class FoodItem (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
)