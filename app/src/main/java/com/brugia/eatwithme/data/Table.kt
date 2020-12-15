
package com.brugia.eatwithme.data

import androidx.annotation.DrawableRes
import java.time.LocalDateTime


data class Table(
    val id: Long,
    var name: String,
    var description: String,
    @DrawableRes
    val image: Int?,
    //var tableDate: LocalDateTime,
    var tableDate: String,
    var tableHour: String,
    var maxPartecipants: Int,
    var numPartecipants: Int,
    var city: String,
    //var partecipants: ArrayList<Person>
)