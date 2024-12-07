fun suggestCalorieIntake(
    calories: Int,
    fat: Int,
    protein: Int,
    totalCaloriesNeeded: Int
): List<String> {
    // Convert fat and protein to calories
    val fatCalories = fat * 9
    val proteinCalories = protein * 4

    // Calculate percentages
    val fatPercentage = (fatCalories.toDouble() / calories) * 100
    val proteinPercentage = (proteinCalories.toDouble() / calories) * 100

    // Create a list for suggestions
    val suggestions = mutableListOf<String>()

    // Caloric intake suggestions
    if (calories < 0.8 * totalCaloriesNeeded) {
        suggestions.add("Increase your intake with nutrient-dense foods like nuts, avocados, or lean protein.")
    } else if (calories > 1.2 * totalCaloriesNeeded) {
        suggestions.add("Consider reducing your calorie intake or increasing physical activity.")
    }

    // Fat percentage suggestions
    when {
        fatPercentage < 20 -> suggestions.add("Add healthy fats like olive oil, nuts, or fatty fish.")
        fatPercentage > 35 -> suggestions.add("Reduce your fat intake and opt for leaner meals.")
    }

    // Protein percentage suggestions
    when {
        proteinPercentage < 10 -> suggestions.add("Increase your protein intake with chicken, tofu, or legumes.")
        proteinPercentage > 35 -> suggestions.add("Balance your diet with more carbs or fats.")
    }

    return suggestions
}