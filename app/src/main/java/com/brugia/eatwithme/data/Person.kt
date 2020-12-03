/*
* Class that represents persons
*/

package com.brugia.eatwithme.data

import java.util.*
import kotlin.collections.ArrayList


data class Person constructor(
    val id: Long,
    val name: String,
    val surname: String,
    val telephone: String,
    val email: String,
    val birthday: Date,
    val preferences: ArrayList<String>
)