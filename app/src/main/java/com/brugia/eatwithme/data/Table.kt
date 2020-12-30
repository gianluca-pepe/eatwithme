
package com.brugia.eatwithme.data

import androidx.annotation.DrawableRes
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


data class Table(
    var id: String? = null,
    var ownerId: String? = null,
    var name: String? = null,
    var description: String? = null,
    @DrawableRes
    val image: Int? = null,
    var timestamp: Timestamp? = null,
    var participants: HashMap<String, Int> = hashMapOf(
            "num" to 0,
            "max" to 0,
    ),
    var location: HashMap<String, Any?> = hashMapOf(
        "latlog" to GeoPoint(0.0, 0.0),
        "label" to null
    )
    //var partecipants: ArrayList<Person>
) {

    private val tableDate: Date?
        get() = timestamp?.let { Date(it.seconds*1000) }

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
        if ( participants["num"] == null )
            return true

        if ( participants["max"] == null )
            return true

        return participants["num"]!! >= participants["max"]!!
    }
}