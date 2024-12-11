package com.cs407.grouplab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cs407.grouplab.CaloriesRecord
import com.cs407.grouplab.CaloriesRecordDao
import com.cs407.grouplab.Exercise
import com.cs407.grouplab.ExerciseDao
import com.cs407.grouplab.ExerciseLog
import com.cs407.grouplab.ExerciseLogDao
import com.cs407.grouplab.FoodItem
import com.cs407.grouplab.FoodItemDao
import com.cs407.grouplab.R
import com.cs407.grouplab.StepRecord
import com.cs407.grouplab.StepRecordDao
import com.cs407.grouplab.UserNutritionLog
import com.cs407.grouplab.UserNutritionLogDao
import com.cs407.grouplab.UserGoal
import com.cs407.grouplab.UserGoalDao
import kotlinx.coroutines.runBlocking
import java.util.Date

//entities- table FoodItem added here
//version- database version


//this is the database class which is used to create the database
@Database(
    entities = [
        FoodItem::class,
        UserNutritionLog::class,
        UserGoal::class,
        StepRecord::class,
        CaloriesRecord::class,
        Exercise::class,
        ExerciseLog::class
    ],
    version = 4, 
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    //dao is used to access the database
    abstract fun foodItemDao(): FoodItemDao
    abstract fun userNutritionLogDao(): UserNutritionLogDao
    abstract fun userGoalDao(): UserGoalDao
    abstract fun stepRecordDao(): StepRecordDao
    abstract fun caloriesRecordDao(): CaloriesRecordDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseLogDao(): ExerciseLogDao

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
                    AppDatabase::class.java,
                    context.getString(R.string.app_database)
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