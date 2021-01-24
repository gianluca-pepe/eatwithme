package com.brugia.eatwithme.placeapi

import com.brugia.eatwithme.data.Restaurant
import com.google.gson.annotations.SerializedName

/**
 * Data class encapsulating a response from the nearby search call to the Places API.
 */
data class NearbyPlacesResponse(
    @SerializedName("results") val results: List<Restaurant>
)