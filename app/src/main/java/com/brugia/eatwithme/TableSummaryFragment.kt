package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.brugia.eatwithme.tablelist.SelectedTableViewModel

class TableSummaryFragment : Fragment() {
    private lateinit var tableDateTextView: TextView
    private lateinit var tableHourTextView: TextView
    private lateinit var tableCityTextView: TextView
    private lateinit var tableParticipantsTextView: TextView
    private val tableViewModel by activityViewModels<SelectedTableViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableCityTextView = view.findViewById(R.id.table_city)
        tableDateTextView = view.findViewById(R.id.table_date)
        tableHourTextView = view.findViewById(R.id.table_hour)
        tableParticipantsTextView = view.findViewById(R.id.table_num_participants)


        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {
                tableHourTextView.text = it.tableHour
                tableDateTextView.text = it.tableDate
                tableCityTextView.text = it.city
                tableParticipantsTextView.text = "${it.numPartecipants} / ${it.maxPartecipants}"
            }
        })

        view.findViewById<Button>(R.id.join_table_button).setOnClickListener {
            findNavController().navigate(R.id.action_join_table)
        }
    }
}