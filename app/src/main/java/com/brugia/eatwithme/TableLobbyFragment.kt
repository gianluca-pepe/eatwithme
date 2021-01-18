package com.brugia.eatwithme

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.brugia.eatwithme.userlist.PersonsAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class TableLobbyFragment : Fragment() {

    private val tableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_lobby, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Persons list management (RecyclerView) */
        //val personsAdapter = PersonsAdapter()

        //val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_person_list)
        //recyclerView.adapter = personsAdapter

        tableViewModel.personsList.observe(viewLifecycleOwner, {
            //personsAdapter.submitList(it)
        })
    }

}