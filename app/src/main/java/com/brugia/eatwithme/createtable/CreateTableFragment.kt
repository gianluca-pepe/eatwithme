package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.datetimepickers.DatePickerFragment
import com.brugia.eatwithme.datetimepickers.TimePickerFragment
import com.brugia.eatwithme.location.LocationModel
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import java.lang.Integer.parseInt


class CreateTableFragment : Fragment() {

    private val newTableViewModel: CreateTableViewModel = CreateTableViewModel()
    private val locationViewModel by viewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }
    private val datePicker = DatePickerFragment(::onDateSet)
    private val timePicker = TimePickerFragment(::onTimeSet)

    private lateinit var nameInputView: TextInputEditText
    private lateinit var descriptionInputView: TextInputEditText
    private lateinit var maxParticipantsInputView: EditText
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var createTableButton: Button

    private lateinit var location: LocationModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_table, container, false)

        dateTextView = view.findViewById(R.id.newTableDate)
        timeTextView = view.findViewById(R.id.newTableHour)
        nameInputView = view.findViewById(R.id.newTableNameinput)
        descriptionInputView = view.findViewById(R.id.newTableDescrinput)
        maxParticipantsInputView = view.findViewById(R.id.maxParticipantsinput)
        createTableButton = view.findViewById(R.id.createTableButton)

        dateTextView.setOnClickListener { this.showDatePickerDialog() }
        timeTextView.setOnClickListener { this.showTimePickerDialog() }
        createTableButton.setOnClickListener { this.onCreateTable() }

        newTableViewModel.tableLiveData.observe(viewLifecycleOwner, {
            dateTextView.text = it.tableDateText()
            timeTextView.text = it.tableHour()
        })

        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            it?.let {
                location = it
            }
        })

        return view
    }


    private fun showDatePickerDialog() {
        datePicker.show(this.requireActivity().supportFragmentManager, "datePicker")
    }

    private fun showTimePickerDialog() {
        timePicker.show(this.requireActivity().supportFragmentManager, "timePicker")
    }

    private fun onDateSet(year: Int, month: Int, day: Int) {
        // set date in the view model
        newTableViewModel.setDate(year,month,day)
        println("creation table: $location")
    }

    private fun onTimeSet(hour: Int, minutes: Int) {
        // set date in the view model
        newTableViewModel.setDate(hour,minutes)
    }

    private fun onCreateTable() {
        newTableViewModel.createTable(
                nameInputView.text.toString(),
                descriptionInputView.text.toString(),
                parseInt(maxParticipantsInputView.text.toString()),
                location
        )
    }

}