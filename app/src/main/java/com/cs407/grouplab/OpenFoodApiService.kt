package com.cs407.grouplab

// OpenFoodApiService.kt
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class OpenFoodProduct(
    val product_name: String?,
    val nutriments: Nutriments?
)

data class Nutriments(
    val energy_kcal: Float?,
    val energy_100g: Float?,
    val energy: Float?,
    val proteins: Float?,
    val carbohydrates: Float?,
    val fat: Float?,
    val saturated_fat: Float?,
    val trans_fat: Float?,
    val fiber: Float?,
    val sugars: Float?,
    val sodium: Float?,
    val potassium: Float?,
    val calcium: Float?,
    val iron: Float?,
    val vitamin_a: Float?,
    val vitamin_c: Float?,
    val vitamin_d: Float?,
    val cholesterol: Float?
)

data class OpenFoodResponse(
    val status: Int,
    val product: OpenFoodProduct?
)

interface OpenFoodApiService {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): Response<OpenFoodResponse>

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/"

        fun create(): OpenFoodApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenFoodApiService::class.java)
        }
    }
}

// FoodItemMapper.kt
fun OpenFoodProduct.toFoodItem(): FoodItem {
    val calories = when {
        this.nutriments?.energy_kcal != null && this.nutriments.energy_kcal > 0 -> 
            this.nutriments.energy_kcal.toInt()
        this.nutriments?.energy_100g != null && this.nutriments.energy_100g > 0 -> 
            this.nutriments.energy_100g.toInt()
        this.nutriments?.energy != null && this.nutriments.energy > 0 -> 
            (this.nutriments.energy / 4.184).toInt()  // Convert kJ to kcal if needed
        else -> 0
    }

    return FoodItem(
        name = this.product_name ?: "Unknown",
        calories = calories,
        protein = this.nutriments?.proteins?.toInt() ?: 0,
        carbs = this.nutriments?.carbohydrates?.toInt() ?: 0,
        fat = this.nutriments?.fat?.toInt() ?: 0,
        saturatedFat = this.nutriments?.saturated_fat?.toInt() ?: 0,
        transFat = this.nutriments?.trans_fat?.toInt() ?: 0,
        polyUnsaturatedFat = 0, // Not available in API
        monoUnsaturatedFat = 0, // Not available in API
        cholesterol = this.nutriments?.cholesterol?.toInt() ?: 0,
        sodium = this.nutriments?.sodium?.toInt() ?: 0,
        potassium = this.nutriments?.potassium?.toInt() ?: 0,
        fiber = this.nutriments?.fiber?.toInt() ?: 0,
        sugar = this.nutriments?.sugars?.toInt() ?: 0,
        vitaminA = this.nutriments?.vitamin_a?.toInt() ?: 0,
        vitaminB = 0, // Not available in API
        vitaminC = this.nutriments?.vitamin_c?.toInt() ?: 0,
        vitaminD = this.nutriments?.vitamin_d?.toInt() ?: 0,
        calcium = this.nutriments?.calcium?.toInt() ?: 0,
        iron = this.nutriments?.iron?.toInt() ?: 0
    )
}