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


    private var placeDetailService: PlaceDetailService= PlaceDetailService.create()
    private var restaurant: Restaurant? = null

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

    fun createTable(name:String, descr:String, maxParticipants:Int, location: Location? = null, placeID: String? = null) {
        val geoPoint =
                if (location == null) GeoPoint(0.0,0.0)
                else GeoPoint(location.latitude, location.longitude)



        //Obtain lists of restaurants from Places API
        //Get restaurants list within 2 kilometers

        val apiKey = BuildConfig.MAPS_KEY
        /*
        val radiusInMeters = 2000
        if (location != null) {
            placesService.nearbyPlaces(
                    apiKey = apiKey,
                    location = "${location.latitude},${location.longitude}",
                    radiusInMeters = radiusInMeters,
                    placeType = "restaurant"
            ).enqueue(
                    object : Callback<NearbyPlacesResponse> {
                        override fun onFailure(call: Call<NearbyPlacesResponse>, t: Throwable) {
                            Log.e(ContentValues.TAG, "Failed to get nearby places", t)
                        }

                        override fun onResponse(
                                call: Call<NearbyPlacesResponse>,
                                response: Response<NearbyPlacesResponse>
                        ) {
                            if (!response.isSuccessful) {
                                Log.e(ContentValues.TAG, "Failed to get nearby places")
                                return
                            }

                            restaurants = response.body()?.results ?: emptyList()

                            //We have obtained the list of restaurants, we can insert the table inside db
                            println("Retrieved restaurants:" + restaurants)

                            _tableLiveData.value = _tableLiveData.value?.copy(
                                    name = name,
                                    description = descr,
                                    maxParticipants = maxParticipants,
                                    location = hashMapOf(
                                            "label" to "null",
                                            "latlog" to geoPoint
                                    ),
                                    restaurantsList = restaurants!!
                            )

                            _tableLiveData.value?.location?.set("geohash", _tableLiveData.value?.geoHash())

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
            )
        }
        */

        /*Obtain restaurant info given the id*/
        if (placeID != null) {
            placeDetailService.PlaceDetail(
                    apiKey = apiKey,
                    placeID = placeID
            ).enqueue(
                    object : Callback<PlaceDetailResponse> {
                        override fun onFailure(call: Call<PlaceDetailResponse>, t: Throwable) {
                            Log.e(ContentValues.TAG, "Failed to get place informations", t)
                        }

                        override fun onResponse(
                                call: Call<PlaceDetailResponse>,
                                response: Response<PlaceDetailResponse>
                        ) {
                            if (!response.isSuccessful) {
                                Log.e(ContentValues.TAG, "Failed to get nearby places")
                                return
                            }

                            restaurant = response.body()?.result ?: null

                            //We have obtained the list of restaurants, we can insert the table inside db
                            println("Retrieved restaurant:" + restaurant)

                            _tableLiveData.value = _tableLiveData.value?.copy(
                                    name = name,
                                    description = descr,
                                    maxParticipants = maxParticipants,
                                    location = hashMapOf(
                                            "label" to "null",
                                            "latlog" to geoPoint
                                    ),
                                    restaurant = restaurant!!
                            )

                            _tableLiveData.value?.location?.set("geohash", _tableLiveData.value?.geoHash())

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
            )
        }
    }
}