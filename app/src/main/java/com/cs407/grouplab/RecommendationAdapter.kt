package com.cs407.grouplab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecommendationAdapter(
    private val recommendations: List<FoodRecommendation>,
    private val onItemClick: (FoodRecommendation) -> Unit // Click listener for items
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.food_name)
        val servingsTextView: TextView = itemView.findViewById(R.id.servings_count)
        val proteinTextView: TextView = itemView.findViewById(R.id.protein_num)
        val carbsTextView: TextView = itemView.findViewById(R.id.carbs_num)
        val fatTextView: TextView = itemView.findViewById(R.id.fat_num)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recommendation_item, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]
        val food = recommendation.food

        // Set food name and servings
        holder.foodNameTextView.text = food.name
        holder.servingsTextView.text = "Servings: ${recommendation.servings}"

        // Set macro information
        holder.proteinTextView.text = "${food.protein * recommendation.servings}g"
        holder.carbsTextView.text = "${food.carbs * recommendation.servings}g"
        holder.fatTextView.text = "${food.fat * recommendation.servings}g"

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(recommendation)
        }
    }

    override fun getItemCount(): Int = recommendations.size
}
