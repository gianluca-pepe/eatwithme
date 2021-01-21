package com.brugia.eatwithme.createtable

import androidx.lifecycle.LiveData
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
    private var _creationState = MutableLiveData<Boolean>()
    val creationState: LiveData<Boolean>
        get() = _creationState

    private val _tableLiveData: MutableLiveData<Table> = MutableLiveData(Table(
            ownerId = FirebaseAuth.getInstance().currentUser?.uid,
            timestamp = Timestamp(calendar.time),
            participantsList = List(1) { FirebaseAuth.getInstance().currentUser!!.uid},
            maxParticipants = 2,
    ))
    val table: LiveData<Table>
        get() = _tableLiveData


    fun setDate(year: Int, month: Int, day: Int) {
        calendar.set(year,month,day)
        _tableLiveData.value = _tableLiveData.value?.copy(
                timestamp = Timestamp(calendar.time)
        )
    }

    fun setDate(hour: Int, minutes: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minutes)
        _tableLiveData.value = _tableLiveData.value?.copy(
                timestamp = Timestamp(calendar.time)
        )
    }

    fun createTable(name:String, descr:String, maxParticipants:Int, location: LocationModel? = null) {
        val geoPoint =
                if (location == null) GeoPoint(0.0,0.0)
                else GeoPoint(location.latitude, location.longitude)

        _tableLiveData.value = _tableLiveData.value?.copy(
                name = name,
                description = descr,
                maxParticipants = maxParticipants,
                location = hashMapOf(
                        "label" to "null",
                        "latlog" to geoPoint
                )
        )

        _tableLiveData.value?.let {
            db.collection("Tables").add(it).addOnSuccessListener {
                _creationState.value = true
            }.addOnFailureListener { e ->
                _creationState.value = false
                println(e)
            }
        }
    }
}