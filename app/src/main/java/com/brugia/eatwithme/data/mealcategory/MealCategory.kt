package com.brugia.eatwithme.data.mealcategory

data class MealCategory(val id: Int, val name: Int, val image: Int, var nameText:String ="") {
    companion object {
        const val LUNCH: Int = 1
        const val BREAKFAST: Int = 2
        const val DINNER: Int = 3
        const val APERITIF: Int = 4
        const val ALL: Int = 0
    }
}