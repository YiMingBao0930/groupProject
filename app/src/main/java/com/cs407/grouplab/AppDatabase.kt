package com.cs407.grouplab

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

//entities- table FoodItem added here
//version- database version


//this is the database class which is used to create the database
@Database(entities = [FoodItem::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    //dao is used to access the database
    abstract fun foodItemDao(): FoodItemDao
    abstract fun userNutritionLogDao(): UserNutritionLogDao

    /**
    Check INSTANCE -> Not Null -> Return INSTANCE
    |
    V
    Null -> Enter Synchronized Block -> Check INSTANCE Again -> Still Null -> Initialize INSTANCE -> Exit Block -> Return INSTANCE
    |
    V
    Not Null -> Return INSTANCE
     **/

    //static method declaration
    companion object {
        //volatile used to keep the db instance in memory and only 1 in memory at all times
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //getting the database instance
        fun getDatabase(context: Context): AppDatabase {
            //return the instance of the db where app_db is food_database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,"food_database"
                )
                    //if migration fails, create new db
                    .fallbackToDestructiveMigration()
                    //build the db
                    .build()

                //assign the instance to the db
                INSTANCE = instance

                instance
            }

        }

        fun populateInitialData(context: Context) {
            val db = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                // Check if the table is empty
                if (db.foodItemDao().countFoodItems() == 0) {
                    val foods = listOf(
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
                    )
                    // Insert all food items at once
                    db.foodItemDao().insertAll(foods)
                }
            }
        }


    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}