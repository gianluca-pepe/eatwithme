package com.brugia.eatwithme

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import androidx.appcompat.app.AlertDialog

class TableSummaryFragment : Fragment() {
    private lateinit var tableDateTextView: TextView
    private lateinit var tableHourTextView: TextView
    private lateinit var tableCityTextView: TextView
    private lateinit var tableParticipantsTextView: TextView
    private val tableViewModel by activityViewModels<SelectedTableViewModel>()
    private val personViewModel: MyProfileViewModel = MyProfileViewModel()
    init {
        personViewModel.getCurrentPerson()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_summary, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableCityTextView = view.findViewById(R.id.table_city)
        tableDateTextView = view.findViewById(R.id.table_date)
        tableHourTextView = view.findViewById(R.id.table_hour)
        tableParticipantsTextView = view.findViewById(R.id.table_num_participants)


        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {
                tableHourTextView.text = it.tableHour()
                tableDateTextView.text = it.tableDateText()
                tableCityTextView.text = it.location["label"].toString()
                tableParticipantsTextView.text = "${it.numParticipants} / ${it.maxParticipants}"
            }
        })

        view.findViewById<Button>(R.id.join_table_button).setOnClickListener {
            personViewModel.myprofileLiveData.value?.let{
                this.joinTable(view)
            }
        }
    }

    fun joinTable(view: View) {
        val person = personViewModel.myprofileLiveData.value
        if (person == null) return
        val navigateToLobby = {  }
        val navigateToMain = { findNavController().navigate(R.id.mainFragment) }

        if ( person.isProfileIncomplete() ) {
            val alert = AlertDialog.Builder(this.requireContext())
            alert
                .setTitle(R.string.incomplete_account_title)
                .setMessage(R.string.incomplete_account_message)
                .setNegativeButton(R.string.incomplete_account_neg_button) { dialog, which ->
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

    private fun navigateToLobby() {
        findNavController().navigate(R.id.action_join_table)
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.mainFragment)
    }

    private fun observeJoinState() {
        tableViewModel.joinState.observe(viewLifecycleOwner, {
            if (it)
                navigateToLobby()
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
            .setNegativeButton(R.string.incomplete_account_neg_button) { dialog, which ->
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