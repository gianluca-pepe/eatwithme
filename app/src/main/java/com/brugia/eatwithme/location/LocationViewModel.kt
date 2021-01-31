package com.brugia.eatwithme.location

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.*
import java.util.*

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val location = LocationLiveData(application)
    val radius = MutableLiveData(3)
    private var _addressLiveData = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _addressLiveData

    fun setRadius(newRadius: Int) {
        radius.value = newRadius
    }
    fun setLocation(loc: Location?) {
        this.location.setLocationData(loc)
    }

    fun getLocationData() = location

    fun setAddress(address: String?) {
        address?.let {
            _addressLiveData.value = it
        }
    }

    fun forceLocationRequest() { location.requestLocation() }

    companion object {
        fun getCityName(latitude: Double, longitude: Double, context: Context): String {
            val addresses: List<Address>
            val geocoder = Geocoder(context, Locale.getDefault())

            addresses = geocoder.getFromLocation(latitude, longitude,1)

            return addresses[0].locality?: ""
        }

        fun getAddressLine(latitude: Double, longitude: Double, context: Context): String {
            val addresses: List<Address>
            val geocoder = Geocoder(context, Locale.getDefault())

            addresses = geocoder.getFromLocation(latitude, longitude,1)

            return addresses[0].getAddressLine(0)
        }
    }
}

class LocationViewModelFactory(private val context: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(
                    application = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
