package com.brugia.eatwithme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels


import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
/*
import com.brugia.eatwithme.addTable.AddTableActivity
import com.brugia.eatwithme.tableDetail.TableDetailActivity
import com.brugia.eatwithme.addTable.FLOWER_DESCRIPTION
import com.brugia.eatwithme.addTable.FLOWER_NAME
*/
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.location.GpsUtils
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.TablesListViewModel
import com.brugia.eatwithme.tablelist.TablesAdapter
import com.brugia.eatwithme.tablelist.TablesListViewModelFactory


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    lateinit var seek: SeekBar
    lateinit var txtkm: TextView

    private val newTableActivityRequestCode = 1
    private val tablesListViewModel by viewModels<TablesListViewModel> {
        TablesListViewModelFactory(this)
    }

    private val locationViewModel by viewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }
    private lateinit var gpsHandler : GpsUtils
    private val requestLocationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                // Callback called when the user interacts with system dialog requesting permission
                if (isGranted) {
                    // Permission is granted.
                    // retrieve tables using location in locationViewModel
                } else {
                    // retrieve tables using default user's location
                }
            }

    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsHandler = GpsUtils(this.requireActivity())
        // Check GPS settings
        gpsHandler.checkGPS()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* SeekBar management*/
        seek = view.findViewById<SeekBar>(R.id.seekBar)
        txtkm = view.findViewById<TextView>(R.id.txtchilometri)

        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                //Toast.makeText(getActivity(), "Progress is: " +  seek.progress+"/"+seek.max, Toast.LENGTH_SHORT).show()
                //txtkm.text = seek.progress.toString() + " Km"
                locationViewModel.setRadius(seek.progress)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
              //Toast.makeText(getActivity(),  "Progress is: " + seek.progress + "%", Toast.LENGTH_SHORT).show()

                checkLocationPermission()
            }
        })
        /* End SeekBar management*/


        /* Tables list management (RecyclerView) */
        val tablesAdapter = TablesAdapter { table -> adapterOnClick(table) }

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_table_list)
        recyclerView.adapter = tablesAdapter

        tablesListViewModel.tablesLiveData.observe(viewLifecycleOwner, {
            it?.let {
                tablesAdapter.submitList(it as MutableList<Table>)
               // headerAdapter.updateFlowerCount(it.size)
            }
        })

        locationViewModel.radius.observe(viewLifecycleOwner, {
            it?.let {
                txtkm.text = it.toString()
            }
        })

        /*
        val fab: View = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            fabOnClick()
        }
        */
        /* End Tables list management (RecyclerView) */

        val imgMarker = view.findViewById<ImageView>(R.id.imgMarker)
        imgMarker.setOnClickListener {
            this.findNavController().navigate(R.id.mapsFragment)
        }
    }

    /* Opens Table detail when RecyclerView item is clicked. */
    private fun adapterOnClick(table: Table) {
        /*
        val intent = Intent(this, TableDetailActivity()::class.java)
        intent.putExtra(TABLE_ID, table.id)
        startActivity(intent)
        */
        selectedTableViewModel.setSelectedTable(table)
        if (selectedTableViewModel.doesUserParticipate()) {
            this.findNavController().navigate(R.id.tableLobbyFragment)
        } else {
            this.findNavController().navigate(R.id.action_select_table)
        }
    }


    private fun checkLocationPermission() {
        when {
            isPermissionGranted() -> { }
            //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {}
            else -> {
                // You can directly ask for the permission.
                // onRequestPermissionsResult(...) gets the result of this request.
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