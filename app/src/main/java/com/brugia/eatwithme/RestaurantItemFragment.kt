package com.brugia.eatwithme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class RestaurantItemFragment: Fragment() {

    private lateinit var restaurantNameTextView: TextView
    private lateinit var restaurantRatingBar: RatingBar
    private lateinit var restaurantTypeTextView: TextView
    private lateinit var restaurantImageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.restaurant_row_item, container, false)

        restaurantRatingBar = view.findViewById(R.id.restaurant_rating_bar)
        restaurantNameTextView = view.findViewById(R.id.restaurant_name)
        restaurantTypeTextView = view.findViewById(R.id.restaurant_type_tag)
        restaurantImageView = view.findViewById(R.id.restaurant_img)

        /**
         * TODO: MANAGING RESTAURANTS THROUGH GOOGLE API
         */
        restaurantNameTextView.text = "La Grotta"
        restaurantTypeTextView.text = "Cucina Italiana"
        restaurantRatingBar.rating = 3.2F
        restaurantImageView.setImageResource(R.drawable.logo_login)

        return view
    }
}