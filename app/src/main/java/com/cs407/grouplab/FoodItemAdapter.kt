package com.cs407.grouplab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodItemAdapter : RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>() {
    private var items: List<FoodItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        // Make sure the layout used here matches your XML file for item views.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item_view, parent, false)
        return FoodItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun setItems(foodItems: List<FoodItem>) {
        this.items = foodItems
        notifyDataSetChanged()
    }

    class FoodItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodName)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.calories)

        fun bind(foodItem: FoodItem) {
            foodNameTextView.text = foodItem.name
            caloriesTextView.text = "Calories: ${foodItem.calories}"
        }
    }
}