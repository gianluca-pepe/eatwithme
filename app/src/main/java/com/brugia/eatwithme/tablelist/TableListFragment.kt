package com.brugia.eatwithme.tablelist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Table


class TableListFragment : Fragment() {

    lateinit var seek: SeekBar
    lateinit var txtkm: TextView
    lateinit var address: TextView
    private val DEFAULT_RADIUS = 10

    private val newTableActivityRequestCode = 1
    private val tablesListViewModel by activityViewModels<TablesListViewModel> {
        TablesListViewModelFactory(this.requireContext())
    }

    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireActivity().application)
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
        return inflater.inflate(R.layout.fragment_table_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //tablesListViewModel.populate()
        /* SeekBar management*/
        seek = view.findViewById(R.id.seekBar)
        txtkm = view.findViewById(R.id.txtchilometri)
        seek.progress = DEFAULT_RADIUS * 10
        txtkm.text = DEFAULT_RADIUS.toString()

        if (tablesListViewModel.location.value == null) {
            seek.visibility = View.GONE
            txtkm.visibility = View.GONE
        }

        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                //Toast.makeText(getActivity(), "Progress is: " +  seek.progress+"/"+seek.max, Toast.LENGTH_SHORT).show()
                //txtkm.text = seek.progress.toString() + " Km"
                tablesListViewModel.radius.value = seek.progress / 10
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                //Toast.makeText(getActivity(),  "Progress is: " + seek.progress + "%", Toast.LENGTH_SHORT).show()

                //checkLocationPermission()

                println("dovrebbe aggiornare")
                tablesListViewModel.refresh()
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
/*
        tablesListViewModel.location.observe(viewLifecycleOwner, {
            if (!isLoading) {
                isLoading = true
                tablesListViewModel.refresh()
            }
        })
*/
        tablesListViewModel.radius.observe(viewLifecycleOwner, {
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
        tablesListViewModel.address.observe(viewLifecycleOwner, {
            address.text = it
        })

        swipeRefreshLayout = view.findViewById(R.id.swipeAllTablesContainer)
        swipeRefreshLayout.setOnRefreshListener {
            if (!isLoading) {
                isLoading = true
                tablesListViewModel.refresh()
            }
        }

        if (tablesListViewModel.location.value == null) {
            view.findViewById<View>(R.id.locationConstraintLayout).visibility = View.GONE
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
        this.findNavController().navigate(R.id.tableInfoFragment)
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
                        tablesListViewModel.loadMoreTables()
                    }
                }
            }
        })
    }
}