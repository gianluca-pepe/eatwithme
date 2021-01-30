
package com.brugia.eatwithme.homepage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.mealcategory.MealCategory

class MealCategoryAdapter(private val onClick: (MealCategory) -> Unit) :
    ListAdapter<MealCategory, MealCategoryAdapter.MealCategoryViewHolder>(MealCategoryDiffCallback) {

    /* ViewHolder for a MealCategory, takes in the inflated view and the onClick behavior. */
    class MealCategoryViewHolder(itemView: View, val onClick: (MealCategory) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val mealName: TextView = itemView.findViewById(R.id.mealName)
        private val mealImage: ImageView = itemView.findViewById(R.id.mealImage)

        private var currentMealCategory: MealCategory? = null

        init {
            itemView.setOnClickListener {
                currentMealCategory?.let {
                    onClick(it)
                }
            }
        }

        /* Bind table to the respective views */
        fun bind(meal: MealCategory?) {
            if (meal == null) return

            currentMealCategory = meal
            mealName.text = meal.nameText
            mealImage.setImageResource(meal.image)
        }
    }

    /* Creates and inflates view and return TableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_meal_type, parent, false)
        return MealCategoryViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: MealCategoryViewHolder, position: Int) {
        val meal = getItem(position)
        holder.bind(meal)
    }
}

object MealCategoryDiffCallback : DiffUtil.ItemCallback<MealCategory>() {
    override fun areItemsTheSame(oldItem: MealCategory, newItem: MealCategory): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MealCategory, newItem: MealCategory): Boolean {
        return oldItem.name == newItem.name
    }
}