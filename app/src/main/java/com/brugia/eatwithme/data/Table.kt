
package com.brugia.eatwithme.data

import androidx.annotation.DrawableRes
import com.brugia.eatwithme.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.model.Document
import java.lang.Integer.parseInt
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
    var location: HashMap<String, Any?> = hashMapOf(
        "latlog" to GeoPoint(0.0, 0.0),
        "label" to null
    )
    //var partecipants: ArrayList<Person>
) {

    // Create a Table from a QueryDocumentSnapshot
    constructor(doc: QueryDocumentSnapshot) : this() {
        id = doc.id
        ownerId = doc.getString("ownerId")
        name = doc.getString("name")
        description = doc.getString("description")
        timestamp = doc.getTimestamp("timestamp")
        location = hashMapOf(
                "latlog" to doc.getGeoPoint("location.latlog"),
                "label" to doc.getString("location.label")
        )
        maxParticipants = doc.getLong("maxParticipants")?.toInt()
        participantsList = doc.get("participantsList") as List<String>
        image = R.drawable.logo_login
    }

    private val tableDate: Date?
        get() = timestamp?.let { Date(it.seconds*1000) }

    val numParticipants: Int
        get() = participantsList.size

    fun tableHour():String {
        tableDate?.let{
            return SimpleDateFormat("HH:mm").format(it)
        }
        return ""
    }

    fun tableDateText(pattern:String = "MM/dd/yyyy"): String {
        tableDate?.let {
            return SimpleDateFormat(pattern).format(it)
        }
        return ""
    }

    fun isFull():Boolean {
        return maxParticipants!! <= numParticipants
    }
}