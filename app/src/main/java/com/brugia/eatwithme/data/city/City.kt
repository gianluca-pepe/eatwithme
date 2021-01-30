package com.brugia.eatwithme.data.city

import android.location.Location

data class City(val name: Int,
                val latitude: Double,
                val longitude: Double,
                val image: Int,
                var nameText: String = "") {

    val location: Location = Location("")
    init {
        location.latitude = latitude
        location.longitude = longitude
    }
}