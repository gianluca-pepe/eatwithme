/*
* PLACES TYPE
* https://developers.google.com/places/web-service/supported_types
* HOW TO OBTAIN IMAGES FROM REFERENCES?
* https://developers.google.com/places/web-service/photos
* */
package com.brugia.eatwithme.data

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import java.util.*


/**
 * A model describing details about a Place (location, name, type, etc.).
 */
data class Restaurant(
        val place_id: String,
        val name: String,
        val geometry: Geometry,
        val vicinity: String,
        val rating: Float,
        val user_ratings_total: Int,
        val price_level: Int,
        val photos: List<Photo>,
        val formatted_phone_number: String,
        val formatted_address: String,
        val icon: String
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Restaurant) {
            return false
        }
        return this.place_id == other.place_id
    }

    override fun hashCode(): Int {
        return this.place_id.hashCode()
    }
}

data class Geometry(
        val location: GeometryLocation
)

data class GeometryLocation(
        val lat: Double,
        val lng: Double
) {
    val latLng: LatLng
        get() = LatLng(lat, lng)
}

data class Photo(
        val photo_reference: String,
        val height: Int,
        val width: Int,
        val html_attributions: List<String>
)
