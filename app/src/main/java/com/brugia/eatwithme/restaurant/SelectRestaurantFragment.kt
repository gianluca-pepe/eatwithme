package com.brugia.eatwithme.restaurant

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.brugia.eatwithme.BuildConfig
import com.brugia.eatwithme.R
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode


class SelectRestaurantFragment : Fragment() {

    private val AUTOCOMPLETE_REQUEST_CODE = 2
    private lateinit var placeID: String
    private lateinit var placeName: String
    private lateinit var txtRestaurantName: TextView

    private val locationViewModel by activityViewModels<LocationViewModel> {
        LocationViewModelFactory(this.requireActivity().application)
    }

    private val requestLocationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                // Callback called when the user interacts with system dialog requesting permission
                if (isGranted) {
                    // Permission is granted.
                    // retrieve tables using location in locationViewModel
                } else {
                    // retrieve tables using default user's location
                }
            }

    private val callback = OnMapReadyCallback { googleMap ->

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        locationViewModel.getLocationData().observe(viewLifecycleOwner, {
            it?.let {
                val currentPos = LatLng(it.latitude, it.longitude)
                googleMap.addMarker(MarkerOptions().position(currentPos).title("My current position"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos,16.0f))
            }
        })
        setPoiClick(googleMap)
        setMapStyle(googleMap)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_restaurant, container, false)
    }

    private fun setPoiClick(map: GoogleMap) {
        map.clear()
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
                            .snippet("Current selection")
            )
            poiMarker.showInfoWindow()
            placeID = poi.placeId
            placeName = poi.name
            txtRestaurantName.text = poi.name
            println(placeID)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.map_style
                    )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapRestaurant) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        //txtPos = view.findViewById<TextView>(R.id.txtPlaceName)

        txtRestaurantName = view.findViewById<TextView>(R.id.txtRestaurantName)

        // Initialize the SDK
        Places.initialize(this.requireActivity().application, BuildConfig.MAPS_KEY)
        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this.requireActivity().application)


        val btnChangePos = view.findViewById<Button>(R.id.btnChangeRestaurantPos)

        btnChangePos.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this.requireActivity().application)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapRestaurant) as SupportMapFragment?

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        with(place) {
                            println("Lat & lon: $latLng Place: $name, Address: $address, Address components: $addressComponents, ID: $id")
                        }
                        val lat = place.latLng?.latitude
                        val lng = place.latLng?.longitude
                        val latlng: Location = Location("Google Maps");
                        if (lat != null) {
                            latlng.latitude = lat
                        }
                        if (lng != null) {
                            latlng.longitude = lng
                        }
                        //Update position on the map
                        locationViewModel.setLocation(latlng)//Update the location
                        locationViewModel.setAddress(place.address)
                        val customLocation = activity?.getSharedPreferences(
                                getString(R.string.custom_location_file_key),
                                Context.MODE_PRIVATE
                        )
                        customLocation?.edit()?.putFloat(
                                getString(R.string.latitude),
                                latlng.latitude.toFloat()
                        )?.putFloat(
                                getString(R.string.longitude),
                                latlng.longitude.toFloat()
                        )?.apply()
                        mapFragment?.getMapAsync(callback)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
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