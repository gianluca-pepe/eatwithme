package com.brugia.eatwithme.tablelist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.Person
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.location.LocationViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SelectedTableViewModel: ViewModel() {
    private val _selectedTable = MutableLiveData<Table>()
    private var _joinState = MutableLiveData<Boolean>()
    val joinState: LiveData<Boolean>
        get() = _joinState

    private var _personsList = MutableLiveData<MutableList<Person>>()
    private var _personsListTemp = mutableListOf<Person>()
    val personsList: LiveData<MutableList<Person>>
        get() = _personsList

    private val auth_id = Firebase.auth.uid
    private val db = Firebase.firestore

    fun setSelectedTable(table: Table?) {
        table?.let {
            _selectedTable.value = table
            createParticipantsList()
        }
    }

    fun getSelectedTable() = _selectedTable

    fun joinTable() {

        auth_id?.let { auth_id ->
            //Update the document inside DB
            _selectedTable.value?.let {
                db.collection("Tables").document(it.id).update(
                    "participantsList",
                    FieldValue.arrayUnion(auth_id)
                )
                    .addOnSuccessListener { _joinState.value = true }
                    .addOnFailureListener { _joinState.value = false }
            }
        }
    }

    fun doesUserParticipate(): Boolean {
        _selectedTable.value?.let {
            return it.participantsList.contains(auth_id) || it.ownerId == auth_id
        }

        return false
    }

    private fun createParticipantsList() {
        auth_id?.let {
            _personsListTemp = mutableListOf()
            _selectedTable.value?.participantsList?.forEach { id ->
                db.collection("Users").document(id).get().addOnSuccessListener {
                    _personsListTemp.add(Person(
                            id = it.getString("id"),
                            name = it.getString("name"),
                            surname = it.getString("surname"),
                            profile_pic = it.getString("profile_pic")
                    ))

                    _personsList.value = _personsListTemp
                }
            }
        }
    }
}
