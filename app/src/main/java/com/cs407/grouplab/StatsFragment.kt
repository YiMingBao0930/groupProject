package com.cs407.grouplab

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button to navigate to AppHomePageFragment
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
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Distance progress bar
        val distanceProgressBar = view.findViewById<ProgressBar>(R.id.distance_progress_bar)
        distanceProgressBar.max = 10000
        val distanceCurrentProgress = 6000
        ObjectAnimator.ofInt(distanceProgressBar, "progress", distanceCurrentProgress)
            .setDuration(2000)
            .start()
        val distanceText = view.findViewById<TextView>(R.id.distance_text)

        // Protein progress bar
        val proteinProgressBar = view.findViewById<ProgressBar>(R.id.protein_progress_bar)
        proteinProgressBar.max = 10000
        val proteinCurrentProgress = 8000
        ObjectAnimator.ofInt(proteinProgressBar, "progress", proteinCurrentProgress)
            .setDuration(2000)
            .start()
        val proteinText = view.findViewById<TextView>(R.id.protein_text)

        // Calories progress bar
        val caloriesProgressBar = view.findViewById<ProgressBar>(R.id.calories_progress_bar)
        caloriesProgressBar.max = 10000
        val caloriesCurrentProgress = 7000
        ObjectAnimator.ofInt(caloriesProgressBar, "progress", caloriesCurrentProgress)
            .setDuration(2000)
            .start()
        val caloriesText = view.findViewById<TextView>(R.id.calories_text)

        // Fat progress bar
        val fatProgressBar = view.findViewById<ProgressBar>(R.id.fat_progress_bar)
        fatProgressBar.max = 10000
        val fatCurrentProgress = 10000
        ObjectAnimator.ofInt(fatProgressBar, "progress", fatCurrentProgress)
            .setDuration(2000)
            .start()
        val fatText = view.findViewById<TextView>(R.id.fat_text)
    }
}
