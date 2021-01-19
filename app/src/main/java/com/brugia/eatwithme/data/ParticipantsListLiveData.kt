package com.brugia.eatwithme.data

import androidx.lifecycle.LiveData
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
        } else {
            println(error)
        }
    }

    fun add (personRef: DocumentReference) {
        documentReferences.add(personRef)
        personRef.addSnapshotListener(this)
    }

    private fun commit () {
        value = participantsListTemp
    }

    fun clear () {
        participantsListTemp.clear()
        documentReferences.clear()
    }
}