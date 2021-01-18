package com.brugia.eatwithme.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.mytables.PastTables
import com.brugia.eatwithme.tablelist.TablesListViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException

class ParticipantsListLiveData(): LiveData<List<Person>>(), EventListener<DocumentSnapshot> {
    private var documentReferences = mutableListOf<DocumentReference>()
    private val participantsListTemp = arrayListOf<Person>()
    var max: Int = 0

    /**
     * Called when a new document snapshot of a person is retrieved
     * add the person in the temporary list
     */
    override fun onEvent(snapshot: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (snapshot != null && snapshot.exists()) {
            participantsListTemp.add(Person(snapshot))
            if (participantsListTemp.size >= max)
                commit()
        }
    }

    fun add (personSnapshot: DocumentReference) {
        documentReferences.add(personSnapshot)
        personSnapshot.addSnapshotListener(this)
    }

    private fun commit () {
        value = participantsListTemp
        println(value)
    }

    fun clear () {
        participantsListTemp.clear()
        documentReferences.clear()
    }
}