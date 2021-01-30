
package com.brugia.eatwithme.data

import androidx.annotation.DrawableRes
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.mealcategory.MealCategory
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.core.GeoHash
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


data class Table(
    var id: String = "",
    var ownerId: String? = null,
    var name: String? = null,
    var description: String? = null,
    @DrawableRes
    var image: Int? = 0,
    var timestamp: Timestamp? = null,
    var maxParticipants: Int? = 0,
    var participantsList: List<String> = emptyList(),
    var restaurant: Restaurant? = null,
    var geoHash: String? = null,
) {

    // Create a Table from a QueryDocumentSnapshot
    constructor(doc: QueryDocumentSnapshot) : this() {
        id = doc.id
        ownerId = doc.getString("ownerId")
        name = doc.getString("name")
        description = doc.getString("description")
        timestamp = doc.getTimestamp("timestamp")
        maxParticipants = doc.getLong("maxParticipants")?.toInt()
        participantsList = doc.get("participantsList") as List<String>
        image = R.drawable.logo_login
        restaurant = Restaurant(doc.get("restaurant") as HashMap<*, *>)
        geoHash = doc.getString("geoHash")
    }

    //@Exclude // Exclude from data serialization to firestore
    private var _distance:Double = (-1).toDouble()

    var distance: Double
        @Exclude get() = _distance
        set(value) { _distance = value}

    val distanceText: String
        @Exclude get() {
            if (distance < 0) return ""
            distance?.let {
                if (distance < 1000) {
                    return distance.toString().substring(0,3)
                }
                else if (distance >= 1000) {
                    return distance.div(1000).toString().substring(0,3)
                }
            }
            return ""
        }

    private val tableDate: Date?
        get() = timestamp?.let { Date(it.seconds*1000) }

    private val latitude: Double?
        get() = restaurant?.geometry?.location?.lat

    private val longitude: Double?
        get() = restaurant?.geometry?.location?.lat

    val numParticipants: Int
        get() = participantsList.size

    fun tableHourText(): String {
        tableDate?.let{
            return SimpleDateFormat("HH:mm").format(it)
        }
        return ""
    }

    fun tableDateText(pattern: String = "MM/dd/yyyy"): String {
        tableDate?.let {
            return SimpleDateFormat(pattern).format(it)
        }
        return ""
    }

    fun isFull (): Boolean {
        return maxParticipants!! <= numParticipants
    }

    fun geoHash(): String {
        if (latitude != null && longitude != null)
            return GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude!!, longitude!!))

        return ""
    }

    fun getCategory(): Int {
        val hour = SimpleDateFormat("HH").format(tableDate).toInt()

        return when(hour) {
            in 12..15 -> MealCategory.LUNCH
            in 19..24 -> MealCategory.DINNER
            in 1..10 -> MealCategory.BREAKFAST
            else -> MealCategory.APERITIF
        }
    }
}