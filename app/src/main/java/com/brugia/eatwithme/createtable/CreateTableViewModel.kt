package com.brugia.eatwithme.createtable

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.location.LocationModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class CreateTableViewModel: ViewModel() {

    private val calendar: Calendar = Calendar.getInstance()
    private val db = Firebase.firestore

    val tableLiveData: MutableLiveData<Table> = MutableLiveData(Table(
            ownerId = FirebaseAuth.getInstance().currentUser?.uid,
            timestamp = Timestamp(calendar.time),
            participants = hashMapOf(
                    "num" to 1,
                    "max" to 10
            )
    ))

    fun setDate(year: Int, month: Int, day: Int) {
        calendar.set(year,month,day)
        tableLiveData.value = tableLiveData.value?.copy(
                timestamp = Timestamp(calendar.time)
        )
    }

    fun setDate(hour: Int, minutes: Int) {
        calendar.set(Calendar.HOUR, hour)
        calendar.set(Calendar.MINUTE, minutes)
        tableLiveData.value = tableLiveData.value?.copy(
                timestamp = Timestamp(calendar.time)
        )
    }

    fun createTable(name:String, descr:String, maxParticipants:Int, location: LocationModel? = null) {
        val geoPoint =
                if (location == null) GeoPoint(0.0,0.0)
                else GeoPoint(location.latitude, location.longitude)

        tableLiveData.value = tableLiveData.value?.copy(
                name = name,
                description = descr,
                participants = hashMapOf(
                        "num" to 1,
                        "max" to maxParticipants
                ),
                location = hashMapOf(
                        "label" to "null",
                        "latlog" to geoPoint
                )
        )

        tableLiveData.value?.let {
            db.collection("Tables").add(it)
        }
    }
}