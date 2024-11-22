package com.cs407.grouplab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class FoodFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to ScanPageFragment
        val signInButton: ImageButton = view.findViewById(R.id.jumptoscanpage)
        signInButton.setOnClickListener {
            val fragment = ScanPageFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Navigate back to AppHomePageFragment
        val lastPageButton: ImageButton = view.findViewById(R.id.back_button)
        lastPageButton.setOnClickListener {
            val fragment = AppHomePageFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Navigate to AddFoodFragment
        val addFoodButton: Button = view.findViewById(R.id.navigateToAddFood)
        addFoodButton.setOnClickListener {
            val fragment = AddFoodFragment()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
