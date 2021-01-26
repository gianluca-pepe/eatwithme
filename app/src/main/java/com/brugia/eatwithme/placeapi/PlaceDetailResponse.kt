package com.brugia.eatwithme.placeapi

import com.brugia.eatwithme.data.Restaurant
import com.google.gson.annotations.SerializedName

data class PlaceDetailResponse(
    @SerializedName("result") val result: Restaurant
)