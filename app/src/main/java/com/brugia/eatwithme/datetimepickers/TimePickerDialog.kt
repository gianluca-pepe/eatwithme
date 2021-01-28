package com.brugia.eatwithme.datetimepickers

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(private val timeSetCallback: (Int, Int) -> Unit) :
    DialogFragment(), TimePickerDialog.OnTimeSetListener {

    var offsetMinutes: Int = 0
    var offsetHours: Int = 0
    var timestamp: Long = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        if (timestamp <= 0 ) {
            // Use the current time as the default values for the picker
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + offsetHours)
            c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + offsetMinutes)
        } else {
            c.timeInMillis = timestamp * 1000
        }

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            activity,
            this,
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        )
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        timeSetCallback(hourOfDay, minute)
    }
}