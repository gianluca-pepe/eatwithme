package com.brugia.eatwithme.location

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import com.brugia.eatwithme.MainFragment
import com.brugia.eatwithme.data.TablesDataSource
import com.brugia.eatwithme.tablelist.TablesListViewModel
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val location = LocationLiveData(application)
    val radius: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    private var _addressLiveData = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _addressLiveData

    fun setRadius(newRadius: Int) {
        radius.value = newRadius
    }
    fun setLocation(loc: Location) {
        this.location.setLocationData(loc)
    }

    fun getLocationData() = location

    fun setAddress(address: String?) {
        address?.let {
            _addressLiveData.value = it
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
