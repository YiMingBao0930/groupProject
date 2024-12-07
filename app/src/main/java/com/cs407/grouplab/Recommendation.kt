package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cs407.grouplab.data.Food
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate


class Recommendation : AppCompatActivity() {
    private val Intake = floatArrayOf(0f, 0f, 0f)
    private val nutritionList = arrayOf("Protein", "Fat", "Carbohydrates")

    private lateinit var appleButton: Button
    private lateinit var bananaButton: Button
    private lateinit var orangeButton: Button
    private lateinit var strawberryButton: Button
    private lateinit var sumEnergyFat: TextView
    private lateinit var sumEnergyProtein: TextView
    private lateinit var sumEnergyCarb: TextView
    private lateinit var ingestionFat: TextView
    private lateinit var ingestionProtein: TextView
    private lateinit var ingestionCarb: TextView
    private lateinit var carbReview: TextView
    private lateinit var fatReview: TextView
    private lateinit var proteinReview: TextView
    private lateinit var fatLight: ImageView
    private lateinit var carbLight: ImageView
    private lateinit var proteinLight: ImageView





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.recommendation)

        // Setup the PieChart after setting the content view
        setupPieChart()

        sumEnergyCarb = findViewById(R.id.sumEnergyCarb)
        sumEnergyFat = findViewById(R.id.sumEnergyFat)
        sumEnergyProtein = findViewById(R.id.sumEnergyProtein)
        ingestionFat = findViewById(R.id.ingestionFat)
        ingestionProtein = findViewById(R.id.ingestionProtein)
        ingestionCarb = findViewById(R.id.ingestionCarb)
        carbReview = findViewById(R.id.carbReview)
        proteinReview = findViewById(R.id.proteinReview)
        fatReview = findViewById(R.id.fatReview)
        fatLight = findViewById(R.id.fatLight)
        proteinLight = findViewById(R.id.proteinLight)
        carbLight = findViewById(R.id.carbLight)


        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            // Navigate to the target activity
            val intent = Intent(this, FoodFragment::class.java)
            startActivity(intent)
        }



        val foodDatabase = listOf(
            Food("Apple", 2,3,4,1,144,36,169,),
            Food("Banana", 9,1,12,3,132,33,146,),
            Food("Orange", 0,0,4,1,64,16,69),
            Food("Strawberry", 0,0,0,0,8,2,8),
        )

        appleButton = findViewById(R.id.apple)
        bananaButton = findViewById(R.id.banana)
        orangeButton = findViewById(R.id.orange)
        strawberryButton = findViewById(R.id.strawberry)

        var fat = 0
        var fatGram = 0
        var protein = 0
        var proteinGram = 0
        var carb = 0
        var carbGram = 0
        var totalCalories = 0
        var pieChartProteinA: Float = 0f
        var pieChartCarbA: Float = 0f
        var pieChartFatA: Float = 0f

        fun updateCarbReview(carbGram: Int, proteinGram: Int, fatGram: Int) {
            when {
                carbGram < 250 -> {
                    carbReview.text = "A deficiency in carbohydrates may cause low blood sugar, dizziness, or even fainting, posing safety risks. It is suggested to consume more plain and low-oil staples like rice, steamed buns, mixed grain rice, or a variety of fresh fruits."
                    carbLight.setImageResource(R.drawable.ic_yellow_circle)
                }
                carbGram in 250..325 -> { // Use Int range
                    carbReview.text = "Moderate carbohydrate intake provides necessary energy for the body. Excellent!"
                    carbLight.setImageResource(R.drawable.ic_green_circle)
                }
                carbGram > 325 -> {
                    carbReview.text = "Too many carbohydrates may result in a calorie surplus and slow down fat loss. Recommendation: Reduce consumption of sweets (including sugary drinks), candied fruits, or staples like rice and steamed buns."
                    carbLight.setImageResource(R.drawable.ic_red_circle)
                }
            }
            when {
                proteinGram < 46 -> {
                    proteinReview.text = "A lack of protein is detrimental to muscle maintenance and repair, which may slow weight loss and lead to quicker rebound. It is advised to eat more lean, lightly stir-fried meats, seafood such as fish, shrimp, and shellfish, or tofu products."
                    proteinLight.setImageResource(R.drawable.ic_yellow_circle)
                }
                proteinGram in 46..56 -> { // Use Int range
                    proteinReview.text = "Reasonable protein intake helps maintain and repair muscles. Well done!"
                    proteinLight.setImageResource(R.drawable.ic_green_circle)
                }
                proteinGram > 56-> {
                    proteinReview.text = "Excess protein intake may cause nutritional imbalance. Recommendation: Eat less lean meat, dairy products, seafood, or tofu."
                    proteinLight.setImageResource(R.drawable.ic_red_circle)
                }
            }
            when {
                fatGram < 46 -> {
                    fatReview.text = "Insufficient fat intake can lead to a lack of essential fatty acids. It is recommended to snack on a handful of plain nuts or have half an avocado as a supplement."
                    fatLight.setImageResource(R.drawable.ic_yellow_circle)
                }
                fatGram in 46..56 -> { // Use Int range
                    fatReview.text = "Adequate fat intake provides essential high-quality fatty acids. Great job!"
                    fatLight.setImageResource(R.drawable.ic_green_circle)
                }
                fatGram > 56-> {
                    fatReview.text = "Excess fat intake can lead to calorie surplus, hinder fat loss, and even cause weight gain. Recommendation: Avoid fried or grilled foods, fatty meats, crispy biscuits, etc."
                    fatLight.setImageResource(R.drawable.ic_red_circle)
                }
            }
        }


        appleButton.setOnClickListener {
            fat += foodDatabase.find { it.name == "Apple" }?.caloriesFromFat ?: 0
            fatGram += foodDatabase.find { it.name == "Apple" }?.gramOfFat ?: 0
            sumEnergyFat.text = fat.toString()
            ingestionFat.text = fatGram.toString()
            val pieChartFatB: Float = foodDatabase.find { it.name == "Apple" }?.caloriesFromFat?.toFloat() ?: 0f
            pieChartFatA += pieChartFatB
            Intake[1] = pieChartFatA
            protein += foodDatabase.find { it.name == "Apple" }?.caloriesFromProtein ?: 0
            proteinGram +=foodDatabase.find { it.name == "Apple" }?.gramOfProtein ?: 0
            sumEnergyProtein.text = protein.toString()
            ingestionProtein.text = proteinGram.toString()
            val pieChartProteinB: Float = foodDatabase.find { it.name == "Apple" }?.caloriesFromProtein?.toFloat() ?: 0f
            pieChartProteinA += pieChartProteinB
            Intake[0] = pieChartProteinA
            carb += foodDatabase.find { it.name == "Apple" }?.caloriesFromCarb ?: 0
            carbGram += foodDatabase.find { it.name == "Apple" }?.gramOfCarb ?: 0
            sumEnergyCarb.text = carb.toString()
            ingestionCarb.text = carbGram.toString()
            val pieChartCarbB: Float = foodDatabase.find { it.name == "Apple" }?.caloriesFromCarb?.toFloat() ?: 0f
            pieChartCarbA += pieChartCarbB
            Intake[2] = pieChartCarbA

            totalCalories += foodDatabase.find { it.name == "Apple" }?.totalCalories ?: 0
            setupPieChart()
            updateCarbReview(carbGram,proteinGram, fatGram)
        }
        bananaButton.setOnClickListener {

            fat += foodDatabase.find { it.name == "Banana" }?.caloriesFromFat ?: 0
            fatGram += foodDatabase.find { it.name == "Banana" }?.gramOfFat ?: 0
            sumEnergyFat.text = fat.toString()
            ingestionFat.text = fatGram.toString()
            val pieChartFatB: Float = foodDatabase.find { it.name == "Banana" }?.caloriesFromFat?.toFloat() ?: 0f
            pieChartFatA += pieChartFatB
            Intake[1] = pieChartFatA
            protein += foodDatabase.find { it.name == "Banana" }?.caloriesFromProtein ?: 0
            proteinGram +=foodDatabase.find { it.name == "Banana" }?.gramOfProtein ?: 0
            sumEnergyProtein.text = protein.toString()
            ingestionProtein.text = proteinGram.toString()
            val pieChartProteinB: Float = foodDatabase.find { it.name == "Banana" }?.caloriesFromProtein?.toFloat() ?: 0f
            pieChartProteinA += pieChartProteinB
            Intake[0] = pieChartProteinA
            carb += foodDatabase.find { it.name == "Banana" }?.caloriesFromCarb ?: 0
            carbGram += foodDatabase.find { it.name == "Banana" }?.gramOfCarb ?: 0
            sumEnergyCarb.text = carb.toString()
            ingestionCarb.text = carbGram.toString()
            val pieChartCarbB: Float = foodDatabase.find { it.name == "Banana" }?.caloriesFromCarb?.toFloat() ?: 0f
            pieChartCarbA += pieChartCarbB
            Intake[2] = pieChartCarbA

            totalCalories += foodDatabase.find { it.name == "Banana" }?.totalCalories ?: 0
            setupPieChart()
            updateCarbReview(carbGram,proteinGram, fatGram)

        }
        orangeButton.setOnClickListener {
            fat += foodDatabase.find { it.name == "Orange" }?.caloriesFromFat ?: 0
            fatGram += foodDatabase.find { it.name == "Orange" }?.gramOfFat ?: 0
            sumEnergyFat.text = fat.toString()
            ingestionFat.text = fatGram.toString()
            val pieChartFatB: Float = foodDatabase.find { it.name == "Orange" }?.caloriesFromFat?.toFloat() ?: 0f
            pieChartFatA += pieChartFatB
            Intake[1] = pieChartFatA
            protein += foodDatabase.find { it.name == "Orange" }?.caloriesFromProtein ?: 0
            proteinGram +=foodDatabase.find { it.name == "Orange" }?.gramOfProtein ?: 0
            sumEnergyProtein.text = protein.toString()
            ingestionProtein.text = proteinGram.toString()
            val pieChartProteinB: Float = foodDatabase.find { it.name == "Orange" }?.caloriesFromProtein?.toFloat() ?: 0f
            pieChartProteinA += pieChartProteinB
            Intake[0] = pieChartProteinA
            carb += foodDatabase.find { it.name == "Orange" }?.caloriesFromCarb ?: 0
            carbGram += foodDatabase.find { it.name == "Orange" }?.gramOfCarb ?: 0
            sumEnergyCarb.text = carb.toString()
            ingestionCarb.text = carbGram.toString()
            val pieChartCarbB: Float = foodDatabase.find { it.name == "Orange" }?.caloriesFromCarb?.toFloat() ?: 0f
            pieChartCarbA += pieChartCarbB
            Intake[2] = pieChartCarbA

            totalCalories += foodDatabase.find { it.name == "Orange" }?.totalCalories ?: 0
            setupPieChart()
            updateCarbReview(carbGram,proteinGram, fatGram)

        }

        strawberryButton.setOnClickListener {
            fat += foodDatabase.find { it.name == "Strawberry" }?.caloriesFromFat ?: 0
            fatGram += foodDatabase.find { it.name == "Strawberry" }?.gramOfFat ?: 0
            sumEnergyFat.text = fat.toString()
            ingestionFat.text = fatGram.toString()
            val pieChartFatB: Float = foodDatabase.find { it.name == "Strawberry" }?.caloriesFromFat?.toFloat() ?: 0f
            pieChartFatA += pieChartFatB
            Intake[1] = pieChartFatA
            protein += foodDatabase.find { it.name == "Strawberry" }?.caloriesFromProtein ?: 0
            proteinGram +=foodDatabase.find { it.name == "Strawberry" }?.gramOfProtein ?: 0
            sumEnergyProtein.text = protein.toString()
            ingestionProtein.text = proteinGram.toString()
            val pieChartProteinB: Float = foodDatabase.find { it.name == "Strawberry" }?.caloriesFromProtein?.toFloat() ?: 0f
            pieChartProteinA += pieChartProteinB
            Intake[0] = pieChartProteinA
            carb += foodDatabase.find { it.name == "Strawberry" }?.caloriesFromCarb ?: 0
            carbGram += foodDatabase.find { it.name == "Strawberry" }?.gramOfCarb ?: 0
            sumEnergyCarb.text = carb.toString()
            ingestionCarb.text = carbGram.toString()
            val pieChartCarbB: Float = foodDatabase.find { it.name == "Strawberry" }?.caloriesFromCarb?.toFloat() ?: 0f
            pieChartCarbA += pieChartCarbB
            Intake[2] = pieChartCarbA

            totalCalories += foodDatabase.find { it.name == "Strawberry" }?.totalCalories ?: 0
            setupPieChart()
            updateCarbReview(carbGram,proteinGram, fatGram)

        }


    }



    private fun setupPieChart() {
        // Populating a list of PieEntries
        val pieEntries = ArrayList<PieEntry>()
        for (i in Intake.indices) {
            pieEntries.add(PieEntry(Intake[i], nutritionList[i]))
        }

        // Creating the data set and setting colors
        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        // Finding the PieChart by its ID and setting data
        val chart = findViewById<PieChart>(R.id.chart) // Use activity's findViewById
        chart.data = PieData(dataSet)
        chart.animateY(2000)
        chart.invalidate() // Refresh chart with new data
    }


}