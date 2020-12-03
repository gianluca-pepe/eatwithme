/*
* Class that represents tables
*/
package com.brugia.eatwithme.data

import java.time.LocalDateTime


class Table constructor(
    val id: Long,
    var name: String,
    var description: String,
    var tableDate: LocalDateTime,
    var maxPartecipants: Int,
    var partecipants: ArrayList<Person>
)