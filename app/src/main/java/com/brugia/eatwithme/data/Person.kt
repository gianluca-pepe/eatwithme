/*
* Class that represents persons
*/

package com.brugia.eatwithme.data

import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.ArrayList


data class Person(
    val id: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    val birthday: String? = null,
    val profile_pic: String? = null,
    val preferences: ArrayList<String>
)