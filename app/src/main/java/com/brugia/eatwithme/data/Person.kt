/*
* Class that represents persons
*/

package com.brugia.eatwithme.data

import com.google.firebase.firestore.DocumentSnapshot
import kotlin.collections.ArrayList


data class Person(
        var id: String? = null,
        var name: String? = null,
        var surname: String? = null,
        var telephone: String? = null,
        var description: String? = null,
        var email: String? = null,
        var birthday: String? = null,
        var profile_pic: String? = null,
        var preferences: ArrayList<String>? = null,
) {
    fun isProfileIncomplete(): Boolean {
        return name.isNullOrEmpty() || surname.isNullOrEmpty() || birthday.isNullOrEmpty()
    }

    /**
     * Build a Person from a DocumentSnapshot from firestore
     */
    constructor(snapshot: DocumentSnapshot) : this() {
        id = snapshot.id
        name = snapshot.getString("name")
        surname = snapshot.getString("surname")
        telephone = snapshot.getString("telephone")
        description = snapshot.getString("description")
        email = snapshot.getString("email")
        birthday = snapshot.getString("birthday")
        profile_pic = snapshot.getString("profile_pic")
        preferences = arrayListOf()
        for(pref in snapshot.get("preferences") as ArrayList<String>) {
            preferences?.add(pref)
        }
    }
}