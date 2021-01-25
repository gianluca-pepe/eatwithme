package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.tablelist.*


class MainFragment : Fragment() {

    lateinit var seek: SeekBar
    lateinit var txtkm: TextView
    lateinit var address: TextView


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
        seek?.progress = locationViewModel.radius.value!! * 10
        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                //Toast.makeText(getActivity(), "Progress is: " +  seek.progress+"/"+seek.max, Toast.LENGTH_SHORT).show()
                //txtkm.text = seek.progress.toString() + " Km"
                locationViewModel.setRadius(seek.progress / 10)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                //Toast.makeText(getActivity(),  "Progress is: " + seek.progress + "%", Toast.LENGTH_SHORT).show()

                //checkLocationPermission()

                println("dovrebbe aggiornare")
                tablesListViewModel.refresh(
                    locationViewModel.getLocationData().value,
                    locationViewModel.radius.value
                )
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
                if (currentList.isNotEmpty() && currentList.last() == null) {
                    currentList.removeLast()
                    tablesAdapter.submitList(currentList)
                }

                swipeRefreshLayout.isRefreshing = false
            }
        })

        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            println("location set")
            println(it)
            if (!isLoading) {
                isLoading = true
                tablesListViewModel.refresh(it, locationViewModel.radius.value)
            }
        })

        locationViewModel.radius.observe(viewLifecycleOwner, {
            it?.let {
                txtkm.text = it.toString() + " km"
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
                tablesListViewModel.refresh(
                    locationViewModel.getLocationData().value,
                    locationViewModel.radius.value
                )
            }
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


    private fun initScrollListener() {
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                /**
                 * if we're not loading anything, we can't scroll down any further and the scrolling
                 * has stopped, then load more tables
                 */
                if ( !isLoading &&
                    !recyclerView.canScrollVertically(1) &&
                    newState==RecyclerView.SCROLL_STATE_IDLE ) {

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (layoutManager.findLastVisibleItemPosition() + 1 == layoutManager.itemCount &&
                            layoutManager.itemCount >= tablesListViewModel.BATCHSIZE) {
                        // show progressBar
                        val currentList = tablesAdapter.currentList.toMutableList()
                        currentList.add(null)
                        tablesAdapter.submitList(currentList)

                        isLoading = true
                        tablesListViewModel.loadMoreTables(
                            locationViewModel.getLocationData().value,
                            locationViewModel.radius.value!!
                        )
                    }
                }
            }
        })
    }
}