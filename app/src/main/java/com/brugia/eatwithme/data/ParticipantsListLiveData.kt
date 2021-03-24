package com.brugia.eatwithme.data

import androidx.lifecycle.LiveData

class ParticipantsListLiveData(): LiveData<List<Person>>() {
    private val participantsListTemp = arrayListOf<Person>()

    fun add (person: Person) {
        participantsListTemp.add(person)
    }

    fun commit () {
        value = participantsListTemp
    }
}