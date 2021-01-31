package com.brugia.eatwithme.homepage

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.BuildConfig
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.data.city.City
import com.brugia.eatwithme.data.citiesList
import com.brugia.eatwithme.data.mealcategory.MealCategory
import com.brugia.eatwithme.data.mealcategory.mealCategories
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.mytables.MyTablesListViewModel
import com.brugia.eatwithme.mytables.MyTablesListViewModelFactory
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.brugia.eatwithme.tablelist.TablesListViewModel
import com.brugia.eatwithme.tablelist.TablesListViewModelFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

val DEFAULT_CITY_RADIUS = 20

class HomepageFragment : Fragment() {
    private val AUTOCOMPLETE_REQUEST_CODE = 2

    private val tablesListViewModel by activityViewModels<TablesListViewModel> {
        TablesListViewModelFactory(this.requireContext())
    }

    private val myTablesListViewModel by activityViewModels<MyTablesListViewModel> {
        MyTablesListViewModelFactory(this.requireContext())
    }
    private val locationViewModel by activityViewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }
    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }

    private lateinit var nearbyTablesContainer: ConstraintLayout
    private lateinit var nearbyTablesRecyclerView: RecyclerView
    private val nearbyTablesAdapter = TablesAdapter { table ->
        onTableClick(table)
    }

    private lateinit var upcomingTablesContainer: ConstraintLayout
    private lateinit var upcomingTablesRecyclerView: RecyclerView
    private val upcomingTablesAdapter = TablesAdapter { table ->
        onTableClick(table)
    }

    private lateinit var mealCategoriesRecyclerView: RecyclerView
    private val mealCategoryAdapter = MealCategoryAdapter { meal ->
        onMealCategoryClick(meal)
    }
    private lateinit var citiesRecyclerView: RecyclerView
    private val cityAdapter = CityAdapter { city -> onCityClick(city) }

    private lateinit var locationCityTextView: TextView
    private lateinit var searchButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        myTablesListViewModel.listenMyTables()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_homepage, container, false)
    }

    override fun onStop() {
        super.onStop()
        myTablesListViewModel.removeListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationCityTextView = view.findViewById(R.id.locationCity)
        nearbyTablesContainer = view.findViewById(R.id.nearbyTables)
        upcomingTablesContainer = view.findViewById(R.id.upcomingTables)

        /**
         * NEARBY TABLES
         */
        nearbyTablesRecyclerView = view.findViewById(R.id.nearbyTablesRecyclerView)
        nearbyTablesRecyclerView.adapter = nearbyTablesAdapter
        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            if ( it != null) {
                // show city name
                locationCityTextView.text = LocationViewModel.getCityName(
                        it.latitude,
                        it.longitude,
                        this.requireContext()
                )
                tablesListViewModel.loadNearby(it)
                nearbyTablesContainer.visibility = View.VISIBLE
            } else {
                locationCityTextView.text = ""
                nearbyTablesContainer.visibility = View.GONE
            }
        })
        view.findViewById<Button>(R.id.nextTablesSeeAll2).setOnClickListener {
            tablesListViewModel.location.value = locationViewModel.getLocationData().value
            tablesListViewModel.radius.value = DEFAULT_CITY_RADIUS
            tablesListViewModel.refresh()
            findNavController().navigate(R.id.action_search)
        }

        tablesListViewModel.nearbyTables.observe(viewLifecycleOwner,{
            it?.let {
                nearbyTablesAdapter.submitList(it)
            }
        })

        /**
         * UPCOMING TABLES
         */
        upcomingTablesRecyclerView = view.findViewById(R.id.upcomingTablesRecyclerView)
        upcomingTablesRecyclerView.adapter = upcomingTablesAdapter
        myTablesListViewModel.myNextTablesLiveData.observe(viewLifecycleOwner, {
            if ( it != null ) {
                upcomingTablesAdapter.submitList(it as MutableList<Table>)
                upcomingTablesContainer.visibility = View.VISIBLE
            } else {
                upcomingTablesContainer.visibility = View.GONE
            }
        })
        view.findViewById<Button>(R.id.nextTablesSeeAll).setOnClickListener {
            findNavController().navigate(R.id.myTablesFragment)
        }

        /**
         * MEAL CATEGORIES
         */
        mealCategoriesRecyclerView = view.findViewById(R.id.mealCategoriesRecyclerView)
        mealCategoriesRecyclerView.adapter = mealCategoryAdapter
        initMealCategoriesRecyclerView()

        /**
         * CITIES
         */
        citiesRecyclerView = view.findViewById(R.id.citiesRecyclerView)
        citiesRecyclerView.adapter = cityAdapter
        initCitiesRecyclerView()

        /**
         * SEARCH PLACES
         */
        Places.initialize(this.requireActivity().application, BuildConfig.MAPS_KEY)
        searchButton = view.findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this.requireActivity().application)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    private fun initMealCategoriesRecyclerView() {
        val meals = mealCategories()
        for (meal in meals) { meal.nameText = getString(meal.name)}
        mealCategoryAdapter.submitList(meals)
    }

    private fun initCitiesRecyclerView() {
        val cities = citiesList()
        for (city in cities) { city.nameText = getString(city.name)}
        cityAdapter.submitList(cities)
    }

    private fun onCityClick(city: City) {
        tablesListViewModel.location.value = city.location
        tablesListViewModel.radius.value = DEFAULT_CITY_RADIUS
        tablesListViewModel.refresh()
        findNavController().navigate(R.id.action_search)
    }

    private fun onMealCategoryClick(category: MealCategory) {
        tablesListViewModel.location.value = null
        tablesListViewModel.radius.value = null
        tablesListViewModel.refresh(mealCategory = category.id)
        findNavController().navigate(R.id.action_search)
    }

    private fun onTableClick(table: Table) {
        selectedTableViewModel.setSelectedTable(table)
        findNavController().navigate(R.id.tableInfoFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        place.latLng?.let {
                            val tempLocation = Location("")
                            tempLocation.latitude = it.latitude
                            tempLocation.longitude = it.longitude
                            tablesListViewModel.location.value = tempLocation
                            tablesListViewModel.radius.value = DEFAULT_CITY_RADIUS
                            tablesListViewModel.refresh()
                            findNavController().navigate(R.id.action_search)
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    Toast.makeText(this.context,R.string.generic_error_message, Toast.LENGTH_SHORT).show()
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        println(status.statusMessage.toString())
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}