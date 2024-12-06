package com.cs407.grouplab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class DailySummaryFragment : Fragment() {

    private val Intake = floatArrayOf(98.8f, 123.8f, 161.6f)
    private val nutritionList = arrayOf("Protein", "Fat", "Carbohydrates")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.daily_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPieChart(view)

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

    private fun setupPieChart(view: View) {
        // Populating a list of PieEntries
        val pieEntries = ArrayList<PieEntry>()
        for (i in Intake.indices) {
            pieEntries.add(PieEntry(Intake[i], nutritionList[i]))
        }

        // Creating the data set and setting colors
        val dataSet = PieDataSet(pieEntries, "Your energy intake today")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        // Creating PieData and setting it to the chart
        val data = PieData(dataSet)
        val chart = view.findViewById<PieChart>(R.id.chart)
        chart.data = data
        chart.animateY(2000)
        chart.invalidate() // Refresh chart with new data
    }
}
