package com.cs407.grouplab

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insert(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("SELECT * FROM food_items WHERE name LIKE :searchQuery ORDER BY name ASC")
    fun searchFoodItems(searchQuery: String): Flow<List<FoodItem>>

    @Insert
    suspend fun insertAll(foodItems: List<FoodItem>)

    // Add this method to count the number of food items in the database
    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun countFoodItems(): Int


}