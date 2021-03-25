package com.brugia.eatwithme.createtable

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.OperationState
import com.brugia.eatwithme.data.Restaurant
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.data.user.UserRepository
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class CreateTableViewModel(val userRepository: UserRepository): ViewModel() {
    private val calendar: Calendar = Calendar.getInstance()
    private val db = Firebase.firestore

    private var _creationState = OperationState()
    val creationState: LiveData<Boolean>
        get() = _creationState
    var editing: Boolean = false
    private val emptyTable = Table(
            ownerId = FirebaseAuth.getInstance().currentUser?.uid,
            timestamp = Timestamp(calendar.time),
            participantsList = List(1) { FirebaseAuth.getInstance().currentUser!!.uid},
            maxParticipants = 2,
            restaurant = null
    )
    private val _tableLiveData: MutableLiveData<Table> = MutableLiveData(emptyTable)
    val table: LiveData<Table>
        get() = _tableLiveData

    var name: String?
        get() = table.value?.name
        set(value) {
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

    fun setTable(table: Table?) {
        _tableLiveData.value = table
        if (table?.timestamp?.seconds != null ) {
            calendar.timeInMillis = table.timestamp?.seconds!!.times(1000)
        }
    }

    fun setDate(year: Int, month: Int, day: Int) {
        println("data cambiata")
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
        if (editing)
            editTable()
        else {
            _tableLiveData.value?.let {
                db.collection("Tables").add(it).addOnSuccessListener { tableRef ->
                    _creationState.firebase = true
                    // create a temp Table to pass the userRepository for performing the request
                    val dummyTable = Table(tableRef.id)
                    userRepository.addParticipantToTable(dummyTable, 1) { state -> _creationState.py = state }
                    it.id = tableRef.id // the table didn't have its id yet. if we don't do this, following requests using this object will fail
                }.addOnFailureListener { e ->
                    _creationState.firebase = false
                    println(e)
                }
            }
        }
    }

    private fun editTable() {
        println(description)
        println(name)
        _tableLiveData.value?.let {
            db.collection("Tables").document(it.id).set(it).addOnSuccessListener {
                _creationState.value = true // in this case we can set value directly because python backend is not affected
            }
        }
    }

    fun reset() {
        _tableLiveData.value = emptyTable
        calendar.timeInMillis = Calendar.getInstance().timeInMillis
    }
}

class CreateTableViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateTableViewModel(
                    userRepository = UserRepository.getUserRepository(application)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}