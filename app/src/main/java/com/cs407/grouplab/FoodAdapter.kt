package com.cs407.grouplab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val foodList = mutableListOf<FoodItem>()

    fun submitList(newList: List<FoodItem>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.food_item_view, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodName.text = foodItem.name
        holder.foodCalories.text = "Calories: ${foodItem.calories}"
    }

    override fun getItemCount(): Int = foodList.size

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.food_name)
        val foodCalories: TextView = itemView.findViewById(R.id.food_calories)
    }
}
