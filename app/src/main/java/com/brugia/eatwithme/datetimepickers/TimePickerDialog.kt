package com.brugia.eatwithme.datetimepickers

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(timeSetCallback: (Int, Int) -> Unit) :
    DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private val timeSetCallback: (Int, Int) -> Unit = timeSetCallback
    var offsetMinutes: Int = 0
    var offsetHours: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + offsetHours)
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + offsetMinutes)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            activity,
            this,
            hour,
            minute,
            true
        )
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        timeSetCallback(hourOfDay, minute)
    }
}