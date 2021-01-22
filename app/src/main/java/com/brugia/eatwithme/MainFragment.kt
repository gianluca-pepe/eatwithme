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
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels


import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
/*
import com.brugia.eatwithme.addTable.AddTableActivity
import com.brugia.eatwithme.tableDetail.TableDetailActivity
import com.brugia.eatwithme.addTable.FLOWER_DESCRIPTION
import com.brugia.eatwithme.addTable.FLOWER_NAME
*/
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.tablelist.*


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    lateinit var seek: SeekBar
    lateinit var txtkm: TextView
    lateinit var address: TextView

    private lateinit var requestLocationPermissionLauncher : ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences

    private val newTableActivityRequestCode = 1
    private val tablesListViewModel by viewModels<TablesListViewModel> {
        TablesListViewModelFactory(this)
    }

    private val locationViewModel by activityViewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }

    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }

    private val tablesAdapter = TablesAdapter { table -> adapterOnClick(table) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isLoading: Boolean = false
    private var endReached: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //gpsHandler = GpsUtils(this.requireActivity())
        // Check GPS settings
        //gpsHandler.checkGPS()
        tablesListViewModel.loadMoreTables()
        requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Callback called when the user interacts with system dialog requesting permission
            if (!isGranted) {
                // retrieve custom location previously stored
                sharedPreferences = this.requireActivity().getSharedPreferences(
                        getString(R.string.custom_location_file_key),
                        AppCompatActivity.MODE_PRIVATE
                )

                val lat = sharedPreferences.getFloat(
                        getString(R.string.latitude),
                        0F
                )
                val long = sharedPreferences.getFloat(
                        getString(R.string.longitude),
                        0F
                )

                // if location has never been stored, ask the user to manually set it
                if (lat == 0F || long == 0F) {
                    AlertDialog.Builder(this.requireContext())
                            .setTitle(R.string.missing_location_title)
                            .setMessage(R.string.missing_location_message)
                            .setPositiveButton(R.string.missing_location_pos_button) { _, _ ->
                                findNavController().navigate(R.id.mapsFragment)
                            }.create().show()
                } else {
                    // manually set location found, restore it in viewmodel
                    val location = Location("")
                    location.latitude = lat.toDouble()
                    location.longitude = long.toDouble()
                    locationViewModel.setLocation(location)
                }
            }
        }
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
        //tablesListViewModel.populate()
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

                //checkLocationPermission()
            }
        })
        /* End SeekBar management*/


        /* Tables list management (RecyclerView) */

        recyclerView = view.findViewById(R.id.recycler_view_table_list)
        recyclerView.adapter = tablesAdapter
        initScrollListener()

        tablesListViewModel.tablesLiveData.observe(viewLifecycleOwner, {
            it?.let {
                tablesAdapter.submitList(it.toMutableList())
                isLoading = false

                if (swipeRefreshLayout.isRefreshing)
                    swipeRefreshLayout.isRefreshing = false
            }
        })

        tablesListViewModel.endReached.observe(viewLifecycleOwner, {
            endReached = it
            if ( endReached ) {
                val currentList = tablesAdapter.currentList.toMutableList()
                isLoading = false
                /**
                 * Remove loading progress bar on end of query reached
                 */
                if (currentList.last() == null) {
                    currentList.removeLast()
                    tablesAdapter.submitList(currentList)
                    println("remove progressbar")
                }

                swipeRefreshLayout.isRefreshing = false
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

        address = view.findViewById(R.id.addressTextView)
        locationViewModel.address.observe(viewLifecycleOwner, {
            address.text = it
        })

        swipeRefreshLayout = view.findViewById(R.id.swipeAllTablesContainer)
        swipeRefreshLayout.setOnRefreshListener {
            if (!isLoading) {
                isLoading = true
                tablesListViewModel.refresh()
                println("refreshing")
            }
        }

        checkLocationPermission()
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

    private fun initScrollListener() {
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!isLoading) {
                    val currentList = tablesAdapter.currentList.toMutableList()
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (layoutManager.findLastVisibleItemPosition() + 1 == layoutManager.itemCount) {
                        currentList.add(null) // show progressBar
                        tablesAdapter.submitList(currentList)

                        isLoading = true
                        tablesListViewModel.loadMoreTables()
                    }
                }
            }
        })
    }
}