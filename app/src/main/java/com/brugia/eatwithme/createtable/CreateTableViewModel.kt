package com.brugia.eatwithme.createtable

import android.content.ContentValues
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brugia.eatwithme.BuildConfig
import com.brugia.eatwithme.data.Restaurant
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.placeapi.NearbyPlacesResponse
import com.brugia.eatwithme.placeapi.PlaceDetailResponse
import com.brugia.eatwithme.placeapi.PlaceDetailService
import com.brugia.eatwithme.placeapi.PlacesService
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            restaurant = null
    ))
    val table: LiveData<Table>
        get() = _tableLiveData

    var name: String?
        get() = table.value?.name
        set(value) {
            val s = "name"
            _tableLiveData.value = _tableLiveData.value?.copy(
                    name = value
            )
        }

    var maxParticipants: Int?
        get() = table.value?.maxParticipants
        set(value) {
            _tableLiveData.value = _tableLiveData.value?.copy(
                    maxParticipants = value
            )
        }

    var description: String?
        get() = table.value?.description
        set(value) {
            _tableLiveData.value = _tableLiveData.value?.copy(
                    description = value
            )
        }

    var restaurant: Restaurant?
        get() = table.value?.restaurant
        set(value) {
            _tableLiveData.value = _tableLiveData.value?.copy(
                    restaurant = value,
                    geoHash = GeoFireUtils.getGeoHashForLocation(GeoLocation(
                            value?.geometry?.location?.lat!!,
                            value?.geometry?.location?.lng!!, // previous line assures us that value != null
                        )
                    )
            )

        }

    val date: Timestamp?
        get() = table.value?.timestamp



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

    fun createTable() {
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