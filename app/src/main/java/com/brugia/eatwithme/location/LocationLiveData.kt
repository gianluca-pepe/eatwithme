package com.brugia.eatwithme.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationLiveData(context: Context) : LiveData<Location?>() {
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    // Stop getting updates of location
    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // First get the last known location, then start receiving updates
    // Called when the number of active observers change from 0 to 1.
    override fun onActive() {
        super.onActive()
        requestLocation()
        //startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        )
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) {
                //context.applicationContext.ac
            } else {
                for (location in locationResult.locations) {
                    setLocationData(location)
                }
            }
        }
    }

    fun setLocationData(location: Location?) {
        value = location
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        println("Last location request")
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    println("success $location")
                    if (location != null) {
                        setLocationData(location)
                    } else {
                        // We never received any location, so the last one is null, perform single
                        // update
                        singleLocationUpdate()
                    }
                }
                .addOnFailureListener {
                    println(it)
                }
    }

    @SuppressLint("MissingPermission")
    private fun singleLocationUpdate() {
        println("single location request")
        fusedLocationClient.requestLocationUpdates(
                singleLocationRequest,
                locationCallback,
                null
        )
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val singleLocationRequest: LocationRequest = LocationRequest.create().apply {
            numUpdates = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
}