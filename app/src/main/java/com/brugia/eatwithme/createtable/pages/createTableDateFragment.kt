package com.brugia.eatwithme.createtable.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ScrollView
import android.widget.Toast
import androidx.compose.ui.unit.minutes
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.createtable.FormPage
import com.brugia.eatwithme.datetimepickers.TimePickerFragment
import com.google.firebase.Timestamp
import java.util.*
import kotlin.properties.Delegates
import kotlin.time.minutes

class CreateTableDateFragment: FormPage() {

    private val newTableViewModel by activityViewModels<CreateTableViewModel>()
    private lateinit var dateInputButton: Button
    private lateinit var timeInputButton: Button
    private lateinit var calendarView: CalendarView
    private var minValidDate: Long = 0
    private val timePicker = TimePickerFragment(::onTimeSet)

    private lateinit var scrollView: ScrollView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.create_table_date, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val c = Calendar.getInstance()
        newTableViewModel.setDate(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE) + 30)
        scrollView = view.findViewById(R.id.scrollView)
        titleTextView.text = getString(R.string.date_title)
        subTitleTextView.text = getString(R.string.date_subtitle)
        calendarView = view.findViewById(R.id.calendarView)
        calendarView.minDate = System.currentTimeMillis() - 1000
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            onDateSet(year, month, dayOfMonth)
        }
        calendarView.visibility = View.GONE
        // set the callback that will scroll down to the calendar when showed
        calendarView.tag = calendarView.visibility
        calendarView.viewTreeObserver.addOnGlobalLayoutListener {
            val newVis: Int = calendarView.visibility
            if (calendarView.tag as Int != newVis) {
                calendarView.tag = calendarView.visibility

                if (calendarView.isVisible)
                    scrollView.smoothScrollTo(0, calendarView.y.toInt() + calendarView.height)
            }
        }

        dateInputButton = view.findViewById(R.id.dateInputButton)
        dateInputButton.setOnClickListener { toggleCalendarView() }
        timeInputButton = view.findViewById(R.id.timeInputButton)
        timeInputButton.setOnClickListener { showTimePickerDialog() }

        newTableViewModel.table.observe(viewLifecycleOwner, { table ->
            val month = table.tableDateText("MMM").capitalize(Locale.ROOT)
            val day = table.tableDateText("d")
            val year = table.tableDateText("yyyy")
            dateInputButton.text = "$day $month $year"
            timeInputButton.text = table.tableHourText()
            table.timestamp?.let {
                timePicker.timestamp = it.seconds
            }
        })
    }

    override fun onStart() {
        super.onStart()
        minValidDate = Timestamp.now().seconds + 1800
        timePicker.timestamp = minValidDate
    }

    private fun toggleCalendarView() {
        if (calendarView.isVisible) {
            calendarView.visibility = View.GONE
        }
        else {
            calendarView.visibility = View.VISIBLE
        }
    }

    private fun showTimePickerDialog() {
        timePicker.show(this.requireActivity().supportFragmentManager, "timePicker")
    }

    private fun onTimeSet(hour: Int, minutes: Int) {
        newTableViewModel.setDate(hour, minutes)
        isValid()
    }

    private fun onDateSet(year: Int, month: Int, day: Int) {
        // set date in the view model
        newTableViewModel.setDate(year, month, day)
    }

    override fun isValid(): Boolean {
        println(newTableViewModel.table.value?.timestamp?.seconds!!)
        println(minValidDate)
        if (newTableViewModel.table.value?.timestamp?.seconds!! < minValidDate) {
            timeInputButton.error = getString(R.string.past_date_error)
            Toast.makeText(this.requireContext(), R.string.past_date_error, Toast.LENGTH_SHORT).show()
            return false
        } else
            timeInputButton.error = null

        return true
    }
}