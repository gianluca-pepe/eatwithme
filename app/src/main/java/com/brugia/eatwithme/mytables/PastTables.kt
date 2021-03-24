package com.brugia.eatwithme.mytables

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.tablelist.*


class PastTables : Fragment() {

    private val myTablesListViewModel by activityViewModels<MyTablesListViewModel> {
        MyTablesListViewModelFactory(this.requireContext())
    }
    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_past_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Tables list management (RecyclerView) */
        val tablesAdapter = TablesAdapter { table -> adapterOnClick(table) }

        //println(tablesAdapter.itemCount)

        val recyclerView: RecyclerView = view.findViewById(R.id.my_past_tables_list)
        recyclerView.adapter = tablesAdapter

        myTablesListViewModel.myPastTablesLiveData.observe(viewLifecycleOwner, {
            it?.let {
                tablesAdapter.submitList(it as MutableList<Table>)
                // headerAdapter.updateFlowerCount(it.size)

            }
        })


        /*
        val fab: View = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            fabOnClick()
        }
        */
        /* End Tables list management (RecyclerView) */

    }

    /* Opens Table detail when RecyclerView item is clicked. */
    private fun adapterOnClick(table: Table) {
        /*
        val intent = Intent(this, TableDetailActivity()::class.java)
        intent.putExtra(TABLE_ID, table.id)
        startActivity(intent)
        */
        selectedTableViewModel.setSelectedTable(table)
        this.findNavController().navigate(R.id.tableInfoFragment)
        /*
        if (selectedTableViewModel.doesUserParticipate()) {
            this.findNavController().navigate(R.id.tableLobbyFragment)
        } else {
            this.findNavController().navigate(R.id.action_select_table)
        }
        */
    }

}