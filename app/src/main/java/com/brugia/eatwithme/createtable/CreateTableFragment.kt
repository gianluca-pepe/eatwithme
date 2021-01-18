package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.datetimepickers.DatePickerFragment
import com.brugia.eatwithme.datetimepickers.TimePickerFragment
import com.brugia.eatwithme.location.LocationModel
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text
import java.lang.Integer.parseInt


class CreateTableFragment : Fragment() {

    private val newTableViewModel: CreateTableViewModel = CreateTableViewModel()
    private val locationViewModel by activityViewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }
    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }
    private val datePicker = DatePickerFragment(::onDateSet)
    private val timePicker = TimePickerFragment(::onTimeSet)

    private lateinit var nameInputView: TextInputEditText
    private lateinit var descriptionInputView: TextInputEditText
    private lateinit var maxParticipantsInputView: EditText
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var createTableButton: Button

    private var location: LocationModel? = null


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

        newTableViewModel.table.observe(viewLifecycleOwner, {
            dateTextView.text = it.tableDateText()
            timeTextView.text = it.tableHour()
            maxParticipantsInputView.setText(it.maxParticipants.toString())
        })

        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            location = it
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
        var err = false
        if (maxParticipantsInputView.text.isNullOrEmpty() || parseInt(maxParticipantsInputView.text.toString()) < 2) {
            //val maxParticipantsLayout = maxParticipantsInputView.parent as TextInputLayout
            maxParticipantsInputView.error = "Almeno 2 partecipanti"
            err = true
        }

        if (nameInputView.text.toString().length <= 3) {
            val nameInputLayout = nameInputView.parent.parent as TextInputLayout
            nameInputLayout.error = "Inserisci un nome lungo almeno 3 caratteri"
            err = true
        }

        if (location == null) {
            AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.missing_location_title)
                    .setMessage(R.string.missing_location_message)
                    .setPositiveButton(R.string.missing_location_pos_button) { _, _ ->
                        findNavController().navigate(R.id.mapsFragment)
                    }.create().show()
            err = true
        }

        if (err) return

        newTableViewModel.createTable(
                nameInputView.text.toString(),
                descriptionInputView.text.toString(),
                parseInt(maxParticipantsInputView.text.toString()),
                location
        )

        newTableViewModel.creationState.observe(viewLifecycleOwner, {
            if ( it == true ) {
                selectedTableViewModel.setSelectedTable(newTableViewModel.table.value)
                findNavController().navigate(R.id.tableLobbyFragment)
            } else {
                AlertDialog.Builder(this.requireContext())
                        .setTitle(R.string.generic_error_title)
                        .setMessage(R.string.generic_error_message)
                        .setPositiveButton(R.string.generic_error_button) { dialog, which ->
                            findNavController().navigate(R.id.mainFragment)
                        }
                        .create().show()
            }
        })
    }

}