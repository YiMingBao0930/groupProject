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
            FoodItem(id = 1, name = "Apple", calories = 95, protein = 0, carbs = 25, fat = 0),
            FoodItem(id = 2, name = "Banana", calories = 105, protein = 1, carbs = 27, fat = 0)
        ).filter { it.name.contains(query, ignoreCase = true) }
    }
}