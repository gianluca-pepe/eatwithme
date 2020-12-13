package com.brugia.eatwithme.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    fun setRadius(newRadius: Int) {
        radius.value = newRadius
    }

    fun getLocationData() = location
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
