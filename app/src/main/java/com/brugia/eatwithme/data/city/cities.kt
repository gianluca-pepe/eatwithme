package com.brugia.eatwithme.data

import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.city.City

fun citiesList(): MutableList<City> {
    return mutableListOf(
        City(
           R.string.rome,
            41.895862679734016, 12.491032819809725,
            R.drawable.rome,
        ),
        City(
            R.string.milan,
            45.47661278622824, 9.188024636540653,
            R.drawable.milan,
        ),
        City(
            R.string.florence,
            43.77085144860714, 11.25381670268658,
            R.drawable.florence,
        ),
        City(
            R.string.naples,
            40.851424044139776, 14.268136128419146,
            R.drawable.naples,
        ),

    )
}