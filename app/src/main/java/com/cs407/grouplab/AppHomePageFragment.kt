package com.cs407.grouplab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppHomePageFragment : Fragment() {
    private val Intake = floatArrayOf(98.8f, 123.8f, 161.6f)
    private val nutritionList = arrayOf("Protein", "Fat", "Carbohydrates")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPieChart(view)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_food -> {
                    val fragment = FoodFragment() // Replace with your FoodFragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_calendar -> {
                    val fragment = DailySummaryFragment() // Replace with your DailySummaryFragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_walk -> {
                    val fragment = ExerciseMenuFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_home -> {
                    val fragment = AppHomePageFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }
                R.id.nav_stats -> {
                    val fragment = StatsFragment() // Replace with your StatsFragment
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }

        val navigationView =
            view.findViewById<com.google.android.material.navigation.NavigationView>(R.id.navigationView)
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.yourGoals -> {
                    val fragment = GoalSettingFragment()
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
            }
        }

        val settingButton: ImageView = view.findViewById(R.id.settingButton)
        settingButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupPieChart(view: View) {
        val pieEntries = ArrayList<PieEntry>()
        for (i in Intake.indices) {
            pieEntries.add(PieEntry(Intake[i], nutritionList[i]))
        }

        val dataSet = PieDataSet(pieEntries, "Your energy intake today")
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.protein_color),
            ContextCompat.getColor(requireContext(), R.color.fat_color),
            ContextCompat.getColor(requireContext(), R.color.carb_color)
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        dataSet.valueTypeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_semibold)

        val data = PieData(dataSet)
        val chart = view.findViewById<PieChart>(R.id.chart)
        chart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.project_background))
        chart.setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.project_background))
        chart.holeRadius = 40f
        chart.legend.isEnabled = false
        chart.data = data
        chart.animateY(2000)
        chart.invalidate()
    }
}
