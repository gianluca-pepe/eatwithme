package com.brugia.eatwithme

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.data.Restaurant
import com.brugia.eatwithme.datetimepickers.DatePickerFragment
import com.brugia.eatwithme.datetimepickers.TimePickerFragment
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import java.lang.Integer.parseInt
import java.util.*


class CreateTableFragment : Fragment() {

    private lateinit var requestLocationPermissionLauncher : ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences

    private val newTableViewModel: CreateTableViewModel = CreateTableViewModel()
    private val locationViewModel by activityViewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }
    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }
    private val datePicker = DatePickerFragment(::onDateSet)
    init {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }
    private val timePicker = TimePickerFragment(::onTimeSet)
    init {
        val c = Calendar.getInstance()
        timePicker.offsetMinutes = 30
        newTableViewModel.setDate(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE) + 30)
    }

    private lateinit var nameInputView: TextInputEditText
    private lateinit var descriptionInputView: TextInputEditText
    private lateinit var maxParticipantsInputView: EditText
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var slctRestaurant: TextView
    private lateinit var createTableButton: Button

    private var location: Location? = null

    private lateinit var placeID: String
    private lateinit var placeName: String
    private lateinit var restaurant: Restaurant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Callback called when the user interacts with system dialog requesting permission
            if (isGranted) locationViewModel.forceLocationRequest()
            else if (!isGranted && locationViewModel.getLocationData().value == null) {
                AlertDialog.Builder(this.requireContext())
                    .setTitle(R.string.missing_location_title)
                    .setMessage(R.string.missing_location_message)
                    .setPositiveButton(R.string.missing_location_pos_button) { _, _ ->
                        findNavController().navigate(R.id.mapsFragment)
                    }
                    .setNegativeButton(R.string.not_now) { _, _ ->
                        findNavController().navigate(R.id.mainFragment)
                    }
                    .create().show()
            }
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_table, container, false)

        dateTextView = view.findViewById(R.id.newTableDate)
        timeTextView = view.findViewById(R.id.newTableHour)
        nameInputView = view.findViewById(R.id.newTableNameinput)
        descriptionInputView = view.findViewById(R.id.newTableDescrinput)
        maxParticipantsInputView = view.findViewById(R.id.maxParticipantsinput)
        maxParticipantsInputView.setText("2")
        createTableButton = view.findViewById(R.id.createTableButton)
        slctRestaurant = view.findViewById(R.id.slctRestaurant)

        dateTextView.setOnClickListener { this.showDatePickerDialog() }
        timeTextView.setOnClickListener { this.showTimePickerDialog() }
        createTableButton.setOnClickListener { this.onCreateTable() }
        slctRestaurant.setOnClickListener { this.addRestaurantFragment() }

        newTableViewModel.table.observe(viewLifecycleOwner, {
            dateTextView.text = it.tableDateText()
            timeTextView.text = it.tableHourText()
        })

        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            location = it
        })
        checkLocationPermission()
        return view
    }

    private fun addRestaurantFragment(){
        findNavController().navigate(R.id.selectRestaurantFragment)
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
    }

    private fun onTimeSet(hour: Int, minutes: Int) {
        // set date in the view model
        newTableViewModel.setDate(hour,minutes)
    }

    private fun onCreateTable() {
        if ( isMaxParticipantsValid() && isNameValid() && isDateValid() && isLocationSet()) {
            newTableViewModel.createTable(
                    nameInputView.text.toString(),
                    descriptionInputView.text.toString(),
                    parseInt(maxParticipantsInputView.text.toString()),
                    location,
                    restaurant
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

    private fun isMaxParticipantsValid(): Boolean {
        if (maxParticipantsInputView.text.isNullOrEmpty() || parseInt(maxParticipantsInputView.text.toString()) < 2) {
            maxParticipantsInputView.error = getString(R.string.max_participants_too_low_error)
            return false
        }

        return true
    }

                private fun isNameValid(): Boolean {
        if (nameInputView.text.toString().length < 3) {
            val nameInputLayout = nameInputView.parent.parent as TextInputLayout
            nameInputLayout.error = getString(R.string.table_name_length_error)
            return false
        }

        return true
    }

    private fun isLocationSet(): Boolean {
        if (location == null) {
            AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.missing_location_title)
                    .setMessage(R.string.missing_location_message)
                    .setPositiveButton(R.string.missing_location_pos_button) { _, _ ->
                        findNavController().navigate(R.id.mapsFragment)
                    }.create().show()
            return false
        }

        return true
    }

    private fun isDateValid(): Boolean {
        if (newTableViewModel.table.value?.timestamp?.seconds!! <= Timestamp.now().seconds + 1800) {
            timeTextView.error = ""
            Toast.makeText(this.requireContext(), R.string.past_date_error, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun checkLocationPermission() {
        when {
            isPermissionGranted() -> { }
            else -> {

                // You can directly ask for the permission.
                // onRequestPermissionsResult(...) gets the result of this request.
                if (locationViewModel.getLocationData().value == null)
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun isPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}