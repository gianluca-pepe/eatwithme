/*
* PLACES TYPE
* https://developers.google.com/places/web-service/supported_types
* HOW TO OBTAIN IMAGES FROM REFERENCES?
* https://developers.google.com/places/web-service/photos
* */
package com.brugia.eatwithme.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A model describing details about a Place (location, name, type, etc.).
 */
data class Restaurant(
        var place_id: String = "",
        var name: String ="",
        var geometry: Geometry? = null,
        var vicinity: String ="",
        var rating: Float = 0F,
        var user_ratings_total: Int = 0,
        var price_level: Int = 0,
        var photos: List<Photo> = listOf(),
        var formatted_phone_number: String = "",
        var formatted_address: String ="",
        var icon: String ="",
) {

    constructor(firestoreRestaurant: HashMap<*,*>) : this() {
        place_id = firestoreRestaurant["place_id"] as String
        name = firestoreRestaurant["name"] as String
        val temp = (firestoreRestaurant["geometry"] as HashMap<*, *>)["location"] as HashMap<*,*>
        geometry = Geometry(GeometryLocation(temp["lat"] as Double, temp["lng"] as Double))
        vicinity = firestoreRestaurant["vicinity"] as String
        rating = (firestoreRestaurant["rating"] as Double).toFloat()
        user_ratings_total = (firestoreRestaurant["user_ratings_total"] as Long).toInt()
        price_level = (firestoreRestaurant["price_level"] as Long).toInt()
        val tempPhotos = mutableListOf<Photo>()
        val temp2 = firestoreRestaurant["photos"] as List<*>
        temp2.forEach {
            val photoMap = it as HashMap<*,*>
            tempPhotos.add(Photo(
                    photo_reference = photoMap["photo_reference"] as String,
                    height = (photoMap["height"] as Long).toInt(),
                    width = (photoMap["width"] as Long).toInt(),
                    html_attributions = listOf() // ignore for now
            ))
        }
        photos = tempPhotos
        formatted_phone_number = firestoreRestaurant["formatted_phone_number"] as String
        formatted_address = firestoreRestaurant["formatted_address"] as String
        icon = firestoreRestaurant["icon"] as String
    }
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
