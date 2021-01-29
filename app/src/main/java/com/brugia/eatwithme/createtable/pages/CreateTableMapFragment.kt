package com.brugia.eatwithme.createtable.pages

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.brugia.eatwithme.BuildConfig
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.createtable.FormPage
import com.brugia.eatwithme.data.Restaurant
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.placeapi.PlaceDetailResponse
import com.brugia.eatwithme.placeapi.PlaceDetailService
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateTableMapFragment : FormPage() {

    private val newTableViewModel by activityViewModels<CreateTableViewModel>()
    private val AUTOCOMPLETE_REQUEST_CODE = 2
    private lateinit var placeID: String
    private lateinit var placeName: String
    private lateinit var txtPos: TextView

    private lateinit var txtct_RestaurantName: TextView
    private lateinit var txtct_RestaurantAddress: TextView
    private lateinit var stella1: ImageView
    private lateinit var stella2: ImageView
    private lateinit var stella3: ImageView
    private lateinit var stella4: ImageView
    private lateinit var stella5: ImageView
    private lateinit var txtct_RestaurantReviewsCount: TextView
    private lateinit var txt_pricelabel: TextView
    private lateinit var txtct_RestaurantPriceLevel: TextView

    private var placeDetailService: PlaceDetailService = PlaceDetailService.create()
    private var restaurant: Restaurant? = null
    val apiKey = BuildConfig.MAPS_KEY

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
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos,18.0f))
            }
        })
        setPoiClick(googleMap)
        setMapStyle(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_table_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.createTableMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        txtPos = view.findViewById<TextView>(R.id.txtct_searchPlace)

        txtct_RestaurantName = view.findViewById<TextView>(R.id.txtct_RestaurantName)
        txtct_RestaurantAddress = view.findViewById<TextView>(R.id.txtct_RestaurantAddress)
        stella1 = view.findViewById<ImageView>(R.id.stella1)
        stella2 = view.findViewById<ImageView>(R.id.stella2)
        stella3 = view.findViewById<ImageView>(R.id.stella3)
        stella4 = view.findViewById<ImageView>(R.id.stella4)
        stella5 = view.findViewById<ImageView>(R.id.stella5)
        txtct_RestaurantReviewsCount = view.findViewById<TextView>(R.id.txtct_RestaurantReviewsCount)
        txt_pricelabel = view.findViewById<TextView>(R.id.txt_pricelabel)
        txtct_RestaurantPriceLevel = view.findViewById<TextView>(R.id.txtct_RestaurantPriceLevel)

        resetTextView()

        //txtRestaurantName = view.findViewById<TextView>(R.id.txtRestaurantName)

        // Initialize the SDK
        Places.initialize(this.requireActivity().application, BuildConfig.MAPS_KEY)
        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this.requireActivity().application)


        val txtct_searchPlace = view.findViewById<TextView>(R.id.txtct_searchPlace)

        txtct_searchPlace.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this.requireActivity().application)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

    }

    private fun setPoiClick(map: GoogleMap) {
        map.clear()
        resetTextView()
        map.setOnPoiClickListener { poi ->

            placeID = poi.placeId
            placeName = poi.name
            /*Looking for place info*/
            /*Obtain restaurant info given the id*/
            if (placeID != null) {
                placeDetailService.PlaceDetail(
                        apiKey = apiKey,
                        placeID = placeID
                ).enqueue(
                        object : Callback<PlaceDetailResponse> {
                            override fun onFailure(call: Call<PlaceDetailResponse>, t: Throwable) {
                                Log.e(ContentValues.TAG, "Failed to get place informations", t)
                            }

                            override fun onResponse(
                                    call: Call<PlaceDetailResponse>,
                                    response: Response<PlaceDetailResponse>
                            ) {
                                if (!response.isSuccessful) {
                                    Log.e(ContentValues.TAG, "Failed to get nearby places")
                                    return
                                }

                                restaurant = response.body()?.result ?: null

                                if(restaurant != null){
                                    //Get place category (understand it from icon name since there isn't a specific field)
                                    /*ES:
                                    * https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png
                                    * https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png
                                    * https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/cafe-71.png
                                    * */
                                    /*var res_category_arr = restaurant!!.icon.split("/")
                                    var res_category = res_category_arr[res_category_arr.lastIndex].replace("-71.png", "")
                                    println(res_category)
                                    //check if the POI regards food & drink
                                    if(res_category == "bar" || res_category == "restaurant" || res_category == "cafe"){*/
                                    //allow adding the marker
                                        map.clear()
                                        resetTextView()
                                        updateRestaurantData(restaurant!!)
                                        val poiMarker = map.addMarker(
                                                MarkerOptions()
                                                        .position(poi.latLng)
                                                        .title(poi.name)
                                                        .snippet("Current selection")
                                        )
                                        poiMarker.showInfoWindow()
                                   /* }else{
                                        Toast.makeText(context,
                                                "Select a restaurant, a bar or a cafe", Toast.LENGTH_SHORT).show()
                                    } */

                                }

                                //We have obtained the list of restaurants, we can insert the table inside db
                                println("Retrieved restaurant:" + restaurant)

                            }
                        }
                )
            }

            //txtRestaurantName.text = poi.name
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
                Log.e(ContentValues.TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(ContentValues.TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val mapFragment = childFragmentManager.findFragmentById(R.id.createTableMap) as SupportMapFragment?

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
                        //Update address label
                        txtPos.text = place.address
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

    private fun resetTextView(){
        nextButton?.visibility = View.INVISIBLE
        txtct_RestaurantName.text = "Place name will appear here"
        txtct_RestaurantAddress.text = "Place address will appear here"
        stella1.visibility = View.INVISIBLE
        stella2.visibility = View.INVISIBLE
        stella3.visibility = View.INVISIBLE
        stella4.visibility = View.INVISIBLE
        stella5.visibility = View.INVISIBLE
        txtct_RestaurantReviewsCount.visibility = View.INVISIBLE
        txt_pricelabel.visibility = View.INVISIBLE
        txtct_RestaurantPriceLevel.visibility = View.INVISIBLE
    }
    //Update restaurant info and add to the
    private fun updateRestaurantData(restaurant: Restaurant){
        nextButton?.visibility = View.VISIBLE
        txtct_RestaurantName.text = restaurant.name
        txtct_RestaurantAddress.text = restaurant.formatted_address

        if(restaurant.price_level != null && restaurant.price_level !=0){
            txt_pricelabel.visibility = View.VISIBLE
            txtct_RestaurantPriceLevel.visibility = View.VISIBLE
            txtct_RestaurantPriceLevel.text = "€".repeat(restaurant.price_level)
        }

        if(restaurant.rating != null && restaurant.user_ratings_total != 0){
            stella1.visibility = View.VISIBLE
            stella2.visibility = View.VISIBLE
            stella3.visibility = View.VISIBLE
            stella4.visibility = View.VISIBLE
            stella5.visibility = View.VISIBLE
            txtct_RestaurantReviewsCount.visibility = View.VISIBLE
            txtct_RestaurantReviewsCount.text = restaurant.rating.toString() + " / " +restaurant.user_ratings_total.toString()

            stella1.setImageResource(R.drawable.ic_baseline_star_border_24)
            stella2.setImageResource(R.drawable.ic_baseline_star_border_24)
            stella3.setImageResource(R.drawable.ic_baseline_star_border_24)
            stella4.setImageResource(R.drawable.ic_baseline_star_border_24)
            stella5.setImageResource(R.drawable.ic_baseline_star_border_24)

            if(restaurant.rating > 4.7 ){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
                stella3.setImageResource(R.drawable.ic_baseline_star_24)
                stella4.setImageResource(R.drawable.ic_baseline_star_24)
                stella5.setImageResource(R.drawable.ic_baseline_star_24)
            }else if (restaurant.rating <= 4.7 && restaurant.rating > 4.2){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
                stella3.setImageResource(R.drawable.ic_baseline_star_24)
                stella4.setImageResource(R.drawable.ic_baseline_star_24)
                stella5.setImageResource(R.drawable.ic_baseline_star_half_24)
            }else if (restaurant.rating <= 4.2 && restaurant.rating > 3.7){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
                stella3.setImageResource(R.drawable.ic_baseline_star_24)
                stella4.setImageResource(R.drawable.ic_baseline_star_24)
            }else if (restaurant.rating <= 3.7 && restaurant.rating > 3.2) {
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
                stella3.setImageResource(R.drawable.ic_baseline_star_24)
                stella4.setImageResource(R.drawable.ic_baseline_star_half_24)
            }else if (restaurant.rating <= 3.2 && restaurant.rating > 2.7){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
                stella3.setImageResource(R.drawable.ic_baseline_star_24)
            }else if (restaurant.rating <= 2.7 && restaurant.rating > 2.2){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
                stella3.setImageResource(R.drawable.ic_baseline_star_half_24)
            }else if (restaurant.rating <= 2.2 && restaurant.rating > 1.7){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_24)
            }else if (restaurant.rating <= 1.7 && restaurant.rating > 1.2){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
                stella2.setImageResource(R.drawable.ic_baseline_star_half_24)
            }else if (restaurant.rating <= 1.2 && restaurant.rating > 0.7){
                stella1.setImageResource(R.drawable.ic_baseline_star_24)
            }else if (restaurant.rating <= 0.7 && restaurant.rating > 0.2) {
                stella1.setImageResource(R.drawable.ic_baseline_star_half_24)
            }else if (restaurant.rating <= 0.2){
                //Do nothing
            }


        }

    }

    private fun isNameValid():Boolean {
        if(restaurant != null) {
            newTableViewModel.restaurant = restaurant
            return true
        }else{
            return false
        }
    }

    //Qui bisogna controllare se il ristorante non e' NULL
    override fun isValid(): Boolean {
        return isNameValid()
    }

}