package com.brugia.eatwithme.location

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient

class GpsUtils(private val context: Context) {

    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
    private var locationSettingsRequest: LocationSettingsRequest?
    //private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager



    init {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(LocationLiveData.locationRequest)
        locationSettingsRequest = builder.build()
        //builder.setAlwaysShow(true)
    }

    fun checkGPS() {
        // settingsChecker is a task, we add listeners to react to failure or success of the request
        val settingsChecker = settingsClient.checkLocationSettings(locationSettingsRequest)

        //settingsChecker.addOnSuccessListener { locationSettingsResponse ->
            // TO DO
        //}

        settingsChecker.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(context as Activity?,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
}

const val REQUEST_CHECK_SETTINGS = 101