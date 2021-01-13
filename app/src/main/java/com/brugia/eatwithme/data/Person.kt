/*
* Class that represents persons
*/

package com.brugia.eatwithme.data

import com.brugia.eatwithme.location.LocationModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList


data class Person(
    val id: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    val birthday: String? = null,
    val profile_pic: String? = null,
    val preferences: ArrayList<String>,
    var default_location: HashMap<String, Any?> = hashMapOf(
            "latlog" to GeoPoint(0.0, 0.0),
            "label" to null
    )
) {
    fun isProfileIncomplete(): Boolean {
        return name.isNullOrEmpty() || surname.isNullOrEmpty() || birthday.isNullOrEmpty()
    }

    fun hasDefaultLocation(): Boolean {
        return default_location["label"] == null
    }
}