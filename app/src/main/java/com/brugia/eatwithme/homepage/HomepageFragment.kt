package com.brugia.eatwithme.homepage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
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

val DEFAULT_CITY_RADIUS = 20

class HomepageFragment : Fragment() {
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

}