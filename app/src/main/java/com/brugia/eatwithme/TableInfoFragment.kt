package com.brugia.eatwithme

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.brugia.eatwithme.userlist.PersonsAdapter

class TableInfoFragment : Fragment() {
    private val tableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }
    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var tableImageView: ImageView

    private lateinit var tableDateTextView: TextView
    private lateinit var tableHourTextView: TextView
    private lateinit var tableCityTextView: TextView
    private lateinit var tableParticipantsTextView: TextView
    private lateinit var btnJoin: Button
    private lateinit var userList: RecyclerView

    private val personViewModel: MyProfileViewModel = MyProfileViewModel()
    init {
        personViewModel.getCurrentPerson()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_info, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView = view.findViewById(R.id.table_name)
        descriptionTextView = view.findViewById(R.id.table_description)
        tableImageView = view.findViewById(R.id.table_image)

        tableCityTextView = view.findViewById(R.id.table_city2)
        tableDateTextView = view.findViewById(R.id.table_date2)
        tableHourTextView = view.findViewById(R.id.table_hour2)
        tableParticipantsTextView = view.findViewById(R.id.table_num_participants2)

        btnJoin = view.findViewById<Button>(R.id.join_table_button2)
        userList = view.findViewById(R.id.recycler_view_person_list)

        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {

                tableHourTextView.text = it.tableHourText()
                tableDateTextView.text = it.tableDateText()
                //tableCityTextView.text = it.location["label"].toString()
                tableParticipantsTextView.text = "${it.numParticipants} / ${it.maxParticipants}"

                nameTextView.text = it.name
                descriptionTextView.text = it.description
                // set image
                val hours = it.tableHourText()
                //Check the hour and set the image according it
                if( hours >= "05:00" && hours < "11:30" ){
                    tableImageView.setImageResource(R.drawable.colazione)
                }else if( hours >= "11:30" && hours < "15:00" ){
                    tableImageView.setImageResource(R.drawable.pranzo)
                }else if( hours >= "19:00" && hours < "22:30" ){
                    tableImageView.setImageResource(R.drawable.cena)
                }else{
                    tableImageView.setImageResource(R.drawable.cocktail)//in every other hours, just a cocktail..
                }
            }
        })

        //Check if the user partecipate to the table, show partecipants list
        if (tableViewModel.doesUserParticipate()) {
            btnJoin.visibility = INVISIBLE
            userList.visibility = VISIBLE
            /* If the person participate to the table, populate the recyclerview*/
            /* Persons list management (RecyclerView) */
            val personsAdapter = PersonsAdapter()
            userList.adapter = personsAdapter

            tableViewModel.personsList.observe(viewLifecycleOwner, {
                personsAdapter.submitList(it)
            })

        } else {
            btnJoin.visibility = VISIBLE
            userList.visibility = INVISIBLE
            /* If the person don't participate to the table, give him/she the possibility to join*/
            btnJoin.setOnClickListener {
                personViewModel.myprofileLiveData.value?.let{
                    this.joinTable(view)
                }
            }
        }
    }

    fun joinTable(view: View) {
        val person = personViewModel.myprofileLiveData.value
        if (person == null) return
        val navigateToMain = { findNavController().navigate(R.id.mainFragment) }

        if ( person.isProfileIncomplete() ) {
            val alert = AlertDialog.Builder(this.requireContext())
            alert
                    .setTitle(R.string.incomplete_account_title)
                    .setMessage(R.string.incomplete_account_message)
                    .setNegativeButton(R.string.not_now) { dialog, which ->
                        /* user refuse to update profile, return to main fragment */
                        navigateToMain()
                    }
                    .setPositiveButton(R.string.incomplete_account_pos_button) { dialog, which ->
                        // user accept to update profile, go to profile settings fragment
                        findNavController().navigate(R.id.myProfileSettingsFragment)
                    }
                    .create().show()
        } else {
            tableViewModel.joinTable()
            observeJoinState()
        }
    }

    private fun navigateToThis() {
        findNavController().navigate(R.id.action_select_table_info)
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.mainFragment)
    }

    private fun observeJoinState() {
        tableViewModel.joinState.observe(viewLifecycleOwner, {
            if (it)
                navigateToThis()
            else {
                showFailureDialog()
                navigateToMain()
            }
        })
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(this.requireContext())
                .setTitle(R.string.incomplete_account_title)
                .setMessage(R.string.incomplete_account_message)
                .setNegativeButton(R.string.not_now) { dialog, which ->
                    /* user refuse to update profile, return to main fragment */
                    this.navigateToMain()
                }
                .setPositiveButton(R.string.incomplete_account_pos_button) { dialog, which ->
                    // user accept to update profile, go to profile settings fragment
                    findNavController().navigate(R.id.myProfileSettingsFragment)
                }
                .create().show()
    }

}