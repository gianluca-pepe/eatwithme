package com.brugia.eatwithme.data.mealcategory


import com.brugia.eatwithme.R

fun mealCategories(): List<MealCategory> {
    return listOf(
            MealCategory(
                    MealCategory.LUNCH,
                    R.string.meal_lunch,
                    R.drawable.lunch,
            ),
            MealCategory(
                    MealCategory.BREAKFAST,
                    R.string.meal_breakfast,
                    R.drawable.breakfast_monochromatic,
            ),
            MealCategory(
                    MealCategory.APERITIF,
                    R.string.meal_cocktail,
                    R.drawable.cocktail_monochromatic,
            ),
            MealCategory(
                    MealCategory.DINNER,
                    R.string.meal_dinner,
                    R.drawable.dinner_monochromatic,
            ),

            )
}