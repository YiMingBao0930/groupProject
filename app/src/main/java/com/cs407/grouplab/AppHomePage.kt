package com.cs407.grouplab

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class AppHomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_home_page)



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_food -> {
                    val intent = Intent(this, NutritionPage::class.java)
                    startActivity(intent)
                    true
                }
                // Handle other menu items
                R.id.nav_calendar -> {
                    // Handle calendar selection
                    true
                }
                R.id.nav_walk -> {
                    // Handle walk selection
                    true
                }
                R.id.nav_home -> {
                    // Handle home selection
                    true
                }
                R.id.nav_stats -> {
                    // Handle stats selection
                    true
                }
                else -> false
            }
        }
    }

}

