package com.cs407.grouplab

import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class MealRecommendation(
    val mealType: String, // Breakfast, Lunch, Snack, Dinner
    val foods: List<FoodRecommendation>,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int
)

data class FoodRecommendation(
    val food: FoodItem,
    var servings: Int
)

suspend fun recommendMealByTime(
    username: String,
    foodItemDao: FoodItemDao,
    userGoalDao: UserGoalDao,
    userNutritionLogDao: UserNutritionLogDao,
    currentTime: Date = Date()
): MealRecommendation? {

    val userGoal = userGoalDao.getUserGoal(username) ?: return null

    val totalCalories = userGoal.dailyCalories
    val calorieTargets = mapOf(
        "Breakfast" to (totalCalories * 0.33).toInt(),
        "Lunch" to (totalCalories * 0.33).toInt(),
        "Dinner" to (totalCalories * 0.33).toInt(),
        "Snack" to 300 // Snacks capped at 300 calories
    )

    val calendar = Calendar.getInstance().apply { time = currentTime }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val mealType = when (hour) {
        in 6..10 -> "Breakfast"
        in 11..13 -> "Lunch"
        in 14..16 -> "Snack"
        in 17..20 -> "Dinner"
        else -> "LateMeal" // For late meals, recommend calorie-dense foods to fill out goal
    }

    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)
    val dailyNutrition = userNutritionLogDao.getDailyNutrition(username, date)

    val remainingCalories = userGoal.dailyCalories - dailyNutrition.totalCalories
    if (remainingCalories <= 0) return null // No more calories to recommend

    // Set weight bias based on meal type
    val (proteinWeight, carbWeight) = when (mealType) {
        "Breakfast" -> 0.3 to 0.6 // More bias towards carbs
        "Dinner" -> 0.6 to 0.3 // More bias towards protein
        else -> 0.5 to 0.5 // Neutral bias
    }

    // Fetch all foods and calculate priority scores
    val allFoods = foodItemDao.searchFoodItems("%").first().map { food ->
        val proteinFactor = food.protein.toDouble() / 50.0 // Normalize to a scale (50g protein max)
        val carbFactor = food.carbs.toDouble() / 50.0 // Normalize (50g carbs max)
        val sugarFactor = (50.0 - food.sugar.toDouble()) / 50.0 // Normalize (low sugar preferred)
        val randomFactor = Random.nextDouble(0.8, 1.2) // Add a small randomization factor
        val priorityScore = randomFactor * (proteinWeight * proteinFactor + carbWeight * carbFactor + 0.4 * sugarFactor)
        food to priorityScore
    }.sortedByDescending { it.second } // Sort foods by priority score

    val targetCalories = when (mealType) {
        "LateMeal" -> remainingCalories // Use all remaining calories for late meals
        "Snack" -> calorieTargets["Snack"]!!.coerceAtMost(remainingCalories) // Snacks capped at remaining
        else -> calorieTargets[mealType]?.coerceAtMost(remainingCalories) ?: 0
    }

    val selectedFoods = mutableListOf<FoodRecommendation>()
    var totalSelectedCalories = 0
    var totalProtein = 0
    var totalCarbs = 0
    var totalFat = 0

    // Decide the number of foods to recommend (1, 2, or 3) with slight bias towards diversity
    val maxFoods = when (Random.nextDouble()) {
        in 0.0..0.4 -> 1 // 40% chance to recommend 1 food (higher servings)
        in 0.4..0.8 -> 2 // 40% chance to recommend 2 foods
        else -> 3 // 20% chance to recommend 3 foods
    }

    // Step 1: Pick up to `maxFoods` with the highest priority scores first
    for ((food, _) in allFoods) {
        if (selectedFoods.size >= maxFoods) break // Limit to `maxFoods` items

        // Start with 1 serving
        val servings = 1
        selectedFoods.add(FoodRecommendation(food, servings))
        totalSelectedCalories += food.calories * servings
        totalProtein += food.protein * servings
        totalCarbs += food.carbs * servings
        totalFat += food.fat * servings

        if (totalSelectedCalories >= targetCalories) break
    }

    // Step 2: Adjust servings for the selected foods to meet target calories
    for (foodRec in selectedFoods) {
        val food = foodRec.food
        val additionalCaloriesNeeded = targetCalories - totalSelectedCalories
        if (additionalCaloriesNeeded <= 0) break

        // Calculate additional servings needed
        val additionalServings = additionalCaloriesNeeded / food.calories
        if (additionalServings > 0) {
            val newServings = foodRec.servings + additionalServings
            totalSelectedCalories += food.calories * additionalServings
            totalProtein += food.protein * additionalServings
            totalCarbs += food.carbs * additionalServings
            totalFat += food.fat * additionalServings

            // Update servings
            foodRec.servings = newServings
        }
    }

    return MealRecommendation(
        mealType = mealType,
        foods = selectedFoods,
        totalCalories = totalSelectedCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat
    )
}






/*
* Populate database for testing and demonstration
 */
suspend fun populateFoodDatabase(foodItemDao: FoodItemDao) {
    // List of foods to add
    val foods = listOf(
        FoodItem(
            name = "Grilled Chicken Breast",
            calories = 165,
            protein = 31,
            carbs = 0,
            fat = 4,
            saturatedFat = 1,
            transFat = 0,
            polyUnsaturatedFat = 1,
            monoUnsaturatedFat = 1,
            cholesterol = 85,
            sodium = 70,
            potassium = 256,
            fiber = 0,
            sugar = 0,
            vitaminA = 0,
            vitaminB = 13,
            vitaminC = 0,
            vitaminD = 0,
            calcium = 10,
            iron = 1
        ),
        FoodItem(
            name = "Brown Rice",
            calories = 215,
            protein = 5,
            carbs = 45,
            fat = 2,
            saturatedFat = 0,
            transFat = 0,
            polyUnsaturatedFat = 1,
            monoUnsaturatedFat = 0,
            cholesterol = 0,
            sodium = 2,
            potassium = 150,
            fiber = 4,
            sugar = 0,
            vitaminA = 0,
            vitaminB = 0,
            vitaminC = 0,
            vitaminD = 0,
            calcium = 20,
            iron = 1
        ),
        FoodItem(
            name = "Avocado",
            calories = 160,
            protein = 2,
            carbs = 9,
            fat = 15,
            saturatedFat = 2,
            transFat = 0,
            polyUnsaturatedFat = 2,
            monoUnsaturatedFat = 10,
            cholesterol = 0,
            sodium = 7,
            potassium = 485,
            fiber = 7,
            sugar = 1,
            vitaminA = 7,
            vitaminB = 20,
            vitaminC = 10,
            vitaminD = 0,
            calcium = 12,
            iron = 1
        ),
        FoodItem(
            name = "Apple",
            calories = 95,
            protein = 0,
            carbs = 25,
            fat = 0,
            saturatedFat = 0,
            transFat = 0,
            polyUnsaturatedFat = 0,
            monoUnsaturatedFat = 0,
            cholesterol = 0,
            sodium = 2,
            potassium = 195,
            fiber = 4,
            sugar = 19,
            vitaminA = 2,
            vitaminB = 1,
            vitaminC = 8,
            vitaminD = 0,
            calcium = 10,
            iron = 0
        ),
        FoodItem(
            name = "Salmon",
            calories = 208,
            protein = 20,
            carbs = 0,
            fat = 13,
            saturatedFat = 3,
            transFat = 0,
            polyUnsaturatedFat = 4,
            monoUnsaturatedFat = 5,
            cholesterol = 55,
            sodium = 75,
            potassium = 485,
            fiber = 0,
            sugar = 0,
            vitaminA = 2,
            vitaminB = 25,
            vitaminC = 0,
            vitaminD = 10,
            calcium = 15,
            iron = 1
        ),
        FoodItem(
            name = "Greek Yogurt",
            calories = 100,
            protein = 10,
            carbs = 6,
            fat = 4,
            saturatedFat = 2,
            transFat = 0,
            polyUnsaturatedFat = 0,
            monoUnsaturatedFat = 1,
            cholesterol = 15,
            sodium = 50,
            potassium = 140,
            fiber = 0,
            sugar = 4,
            vitaminA = 3,
            vitaminB = 7,
            vitaminC = 0,
            vitaminD = 5,
            calcium = 110,
            iron = 0
        ),
        FoodItem(
            name = "Broccoli",
            calories = 55,
            protein = 5,
            carbs = 11,
            fat = 0,
            saturatedFat = 0,
            transFat = 0,
            polyUnsaturatedFat = 0,
            monoUnsaturatedFat = 0,
            cholesterol = 0,
            sodium = 50,
            potassium = 300,
            fiber = 4,
            sugar = 2,
            vitaminA = 12,
            vitaminB = 15,
            vitaminC = 90,
            vitaminD = 0,
            calcium = 50,
            iron = 1
        ),
        FoodItem(
            name = "Quinoa",
            calories = 222,
            protein = 8,
            carbs = 39,
            fat = 4,
            saturatedFat = 0,
            transFat = 0,
            polyUnsaturatedFat = 2,
            monoUnsaturatedFat = 1,
            cholesterol = 0,
            sodium = 13,
            potassium = 318,
            fiber = 5,
            sugar = 1,
            vitaminA = 1,
            vitaminB = 0,
            vitaminC = 0,
            vitaminD = 0,
            calcium = 31,
            iron = 2
        ),
        FoodItem(
            name = "Oatmeal",
            calories = 150,
            protein = 6,
            carbs = 27,
            fat = 3,
            saturatedFat = 0,
            transFat = 0,
            polyUnsaturatedFat = 1,
            monoUnsaturatedFat = 1,
            cholesterol = 0,
            sodium = 1,
            potassium = 150,
            fiber = 4,
            sugar = 1,
            vitaminA = 0,
            vitaminB = 0,
            vitaminC = 0,
            vitaminD = 0,
            calcium = 20,
            iron = 2
        ),
        FoodItem(
            name = "Banana",
            calories = 105,
            protein = 1,
            carbs = 27,
            fat = 0,
            saturatedFat = 0,
            transFat = 0,
            polyUnsaturatedFat = 0,
            monoUnsaturatedFat = 0,
            cholesterol = 0,
            sodium = 1,
            potassium = 422,
            fiber = 3,
            sugar = 14,
            vitaminA = 2,
            vitaminB = 10,
            vitaminC = 17,
            vitaminD = 0,
            calcium = 6,
            iron = 0
        )
    )

    // Insert foods, avoiding duplicates
    for (food in foods) {
        val existingFoods = foodItemDao.searchFoodItems("%${food.name}%").first()
        if (existingFoods.isEmpty()) {
            foodItemDao.insert(food)
        }
    }
}

