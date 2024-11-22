package com.cs407.grouplab

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.grouplab.FoodItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {
    private val repository = FoodRepository() // Assuming you have a repository
    val foodItems = MutableLiveData<List<FoodItem>>()

    fun searchFood(query: String) {
        viewModelScope.launch {
            val results = repository.searchFoodItems(query)
            foodItems.postValue(results)
        }
    }
}

class FoodRepository {
    suspend fun searchFoodItems(query: String): List<FoodItem> {
        // Simulate network/database call
        delay(1000) // Only for demonstration
        return listOf(
            FoodItem(
                name = "Apple",
                calories = 95,
                protein = 0, // g
                carbs = 25, // g
                fat = 0, // g
                saturatedFat = 0, // g
                unsaturatedFat = 0, // g
                cholesterol = 0, // mg
                sodium = 2, // mg
                potassium = 195, // mg
                fiber = 4, // g
                sugar = 19, // g
                vitaminA = 98, // µg
                vitaminB = 0, // mg
                vitaminC = 8, // mg
                vitaminD = 0, // µg
                calcium = 11, // mg
                iron = 0 // mg
            ),
            FoodItem(
                name = "Banana",
                calories = 105,
                protein = 1, // g
                carbs = 27, // g
                fat = 0, // g
                saturatedFat = 0, // g
                unsaturatedFat = 0, // g
                cholesterol = 0, // mg
                sodium = 1, // mg
                potassium = 422, // mg
                fiber = 3, // g
                sugar = 14, // g
                vitaminA = 64, // µg
                vitaminB = 0, // mg
                vitaminC = 10, // mg
                vitaminD = 0, // µg
                calcium = 6, // mg
                iron = 0 // mg
            ),
            FoodItem(
                name = "Carrot",
                calories = 25,
                protein = 1, // g
                carbs = 6, // g
                fat = 0, // g
                saturatedFat = 0, // g
                unsaturatedFat = 0, // g
                cholesterol = 0, // mg
                sodium = 42, // mg
                potassium = 195, // mg
                fiber = 2, // g
                sugar = 3, // g
                vitaminA = 835, // µg
                vitaminB = 0, // mg
                vitaminC = 6, // mg
                vitaminD = 0, // µg
                calcium = 20, // mg
                iron = 0 // mg
            )
        ).filter { it.name.contains(query, ignoreCase = true) }
    }
}