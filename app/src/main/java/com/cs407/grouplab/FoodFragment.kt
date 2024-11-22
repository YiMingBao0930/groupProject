package com.cs407.grouplab

import AddToLogDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.grouplab.data.AppDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoodFragment : Fragment(), FoodItemAdapter.OnItemClickListener {

    private lateinit var searchView: SearchView
    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var addFoodButton: Button
    private lateinit var scanButton: ImageButton
    private val foodItemAdapter = FoodItemAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.food_search_view)
        foodRecyclerView = view.findViewById(R.id.food_recycler_view)
        noResultsTextView = view.findViewById(R.id.no_results_text_view)
        addFoodButton = view.findViewById(R.id.navigateToAddFood)
        scanButton = view.findViewById(R.id.jumptoscanpage)

        foodRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        foodRecyclerView.adapter = foodItemAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { performSearch(it) }
                return true
            }
        })

        // Set up the Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_food)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button in the toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "" // Hide the toolbar title text
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back to the previous fragment
        }

        // Navigate to AddFoodFragment
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
        scanButton.setOnClickListener {
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
    }

    private fun performSearch(query: String) {
        val db = AppDatabase.getDatabase(requireContext())
        val liveDataResults = db.foodItemDao().searchFoodItems("%$query%")

        liveDataResults.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                noResultsTextView.visibility = View.VISIBLE
                foodRecyclerView.visibility = View.GONE
            } else {
                noResultsTextView.visibility = View.GONE
                foodRecyclerView.visibility = View.VISIBLE
                foodItemAdapter.setItems(results)
            }
        }
    }

    override fun onItemClick(foodItem: FoodItem) {
        showAddToLogDialog(foodItem)
    }

    private fun showAddToLogDialog(foodItem: FoodItem) {
        val dialog = AddToLogDialogFragment(foodItem)
        dialog.show(parentFragmentManager, "AddToLogDialogFragment")
    }
}
