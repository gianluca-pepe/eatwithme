package com.brugia.eatwithme.datetimepickers

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(dateSetCallback: (Int, Int, Int) -> Unit):
    DialogFragment(), DatePickerDialog.OnDateSetListener {

    private val dateSetCallback: (Int, Int, Int)-> Unit = dateSetCallback
    var minDate: Long = 0
        set(value) {
            if (value > 0) field = value
        }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        val datePickerDialog = DatePickerDialog(this.requireActivity(), this, year, month, day)
        datePickerDialog.datePicker.minDate = minDate
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val realMonth = month + 1
        // Do something with the date chosen by the user
        dateSetCallback(year, realMonth, day)
    }
}