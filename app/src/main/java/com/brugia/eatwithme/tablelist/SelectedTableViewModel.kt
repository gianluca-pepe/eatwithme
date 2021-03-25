package com.brugia.eatwithme.tablelist

import android.app.Application
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
import com.brugia.eatwithme.data.user.UserRepository
import com.brugia.eatwithme.data.OperationState

class SelectedTableViewModel(val dataSource: UserRepository): ViewModel() {
    private val _selectedTable = MutableLiveData<Table>()
    private var _joinState = OperationState()
    val joinState: LiveData<Boolean>
        get() = _joinState

    private var _exitState = OperationState()
    val exitState: LiveData<Boolean>
        get() = _exitState

    private var _deleteState = OperationState()
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
            getParticipants()
        }
    }

    fun getSelectedTable() = _selectedTable

    fun getParticipants() {
        _selectedTable.value?.let { _personsList = dataSource.getParticipantsOfTable(it) }
    }

    fun joinTable() {

        auth_id?.let {
            //Update the document inside DB
            _selectedTable.value?.let { it ->

                db.collection("Tables").document(it.id).update(
                    "participantsList",
                    FieldValue.arrayUnion(auth_id)
                )
                    .addOnSuccessListener { _joinState.firebase = true }
                    .addOnFailureListener { _joinState.firebase = false }


                dataSource.addParticipantToTable(it, 0) { state -> _joinState.py = state}
            }
        }
    }

    fun doesUserParticipate(): Boolean {
        _selectedTable.value?.let {
            return it.participantsList.contains(auth_id) || it.ownerId == auth_id
        }

        return false
    }

    fun isUserCreator(): Boolean {
        _selectedTable.value?.let {
            return it.ownerId == auth_id
        }

        return false
    }

    fun isTableFull(): Boolean{
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
                    .addOnSuccessListener { _exitState.firebase = true }
                    .addOnFailureListener { _exitState.firebase = false }

            dataSource.deleteParticipantFromTable(it) { state -> _exitState.py = state }
        }
    }

    fun deleteTable(){

        _selectedTable.value?.let {
            db.collection("Tables").document(it.id).delete()
                    .addOnSuccessListener { _deleteState.firebase = true }
                    .addOnFailureListener { _deleteState.firebase = false }

            dataSource.deleteTable(it) { state -> _deleteState.py = state }
        }
    }
}

class SelectedTableViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectedTableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectedTableViewModel(
                    dataSource = UserRepository.getUserRepository(application)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}