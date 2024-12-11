package com.cs407.grouplab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class FoodItemAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(foodItem: FoodItem)
    }

    private var items: List<FoodItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item_view, parent, false)
        return FoodItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            listener.onItemClick(items[position])
        }
    }

    override fun getItemCount() = items.size

    fun setItems(foodItems: List<FoodItem>) {
        this.items = foodItems
        notifyDataSetChanged()
    }

    class FoodItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.food_name)
        private val proteinTextView: TextView = itemView.findViewById(R.id.protein_num)
        private val carbsTextView: TextView = itemView.findViewById(R.id.carbs_num)
        private val fatTextView: TextView = itemView.findViewById(R.id.fat_num)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.food_calories)

        fun bind(foodItem: FoodItem) {
            foodNameTextView.text = foodItem.name
            proteinTextView.text = "${foodItem.protein}g"
            carbsTextView.text = "${foodItem.carbs}g"
            fatTextView.text = "${foodItem.fat}g"
            caloriesTextView.text = "${foodItem.calories} Calories"
        }
    }
}
