package com.cs407.grouplab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.data.AppDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: DailySummaryAdapter
    private var nutritionLogs = mutableListOf<DailyNutritionSummary>()
    private var dates = mutableListOf<Date>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = AppDatabase.getDatabase(requireContext())
        return inflater.inflate(R.layout.daily_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackButton(view)
        setupRecyclerView(view)
        loadWeeklyData()
    }

    private fun setupBackButton(view: View) {
        // Set up back button to navigate to AppHomePageFragment
        val lastPageButton: ImageButton = view.findViewById(R.id.back_button)
        lastPageButton.setOnClickListener {
            val fragment = AppHomePageFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, fragment) // Replace with the ID of your container
                .addToBackStack(null) // Add to back stack for reverse navigation
                .commit()
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.days_recycler_view)
        adapter = DailySummaryAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadWeeklyData() {
        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                nutritionLogs.clear()
                dates.clear()
                
                // Get last 7 days of data
                for (i in 0..6) {
                    val date = calendar.time
                    dates.add(date)
                    
                    val dateStr = dateFormat.format(date)
                    val dailyNutrition = db.userNutritionLogDao()
                        .getDailyNutrition("current_user", dateStr) 
                    
                    nutritionLogs.add(dailyNutrition)
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }
                
                // Update the adapter with new data
                adapter.updateData(nutritionLogs, dates)
                
            } catch (e: Exception) {
                // Handle error (show error message to user)
                e.printStackTrace()
            }
        }
    }
}
