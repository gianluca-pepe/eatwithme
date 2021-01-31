package com.brugia.eatwithme.tablelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.Person
import com.brugia.eatwithme.data.Table
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.brugia.eatwithme.data.TablesDataSource

class SelectedTableViewModel(val dataSource: TablesDataSource): ViewModel() {
    private val _selectedTable = MutableLiveData<Table>()
    private var _joinState = MutableLiveData<Boolean>()
    val joinState: LiveData<Boolean>
        get() = _joinState

    private var _exitState = MutableLiveData<Boolean>()
    val exitState: LiveData<Boolean>
        get() = _exitState

    private var _deleteState = MutableLiveData<Boolean>()
    val deleteState: LiveData<Boolean>
        get() = _deleteState

    private lateinit var _personsList : LiveData<List<Person>>
    val personsList: LiveData<List<Person>>
        get() = _personsList

    private val auth_id = Firebase.auth.uid
    private val db = Firebase.firestore

    fun setSelectedTable(table: Table?) {
        table?.let {
            _selectedTable.value = table
            _personsList = dataSource.getParticipantsList(table)
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

    fun UserIsCreator(): Boolean {
        _selectedTable.value?.let {
            return it.ownerId == auth_id
        }

        return false
    }

    fun doesTableIsFull(): Boolean{
        _selectedTable.value?.let {
            return it.numParticipants == it.maxParticipants
        }

        return false
    }

    fun exitTable(){

        _selectedTable.value?.let {
            db.collection("Tables").document(it.id).update(
                    "participantsList",
                    FieldValue.arrayRemove(auth_id)
            )
                    .addOnSuccessListener { _exitState.value = true }
                    .addOnFailureListener { _exitState.value = false }
        }
    }

    fun deleteTable(){

        _selectedTable.value?.let {
            db.collection("Tables").document(it.id).delete()
                    .addOnSuccessListener { _deleteState.value = true }
                    .addOnFailureListener { _deleteState.value = false }
        }
    }
}

class SelectedTableViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectedTableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectedTableViewModel(
                    dataSource = TablesDataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}