
package com.brugia.eatwithme.homepage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.city.City

class CityAdapter(private val onClick: (City) -> Unit) :
    ListAdapter<City, CityAdapter.CityViewHolder>(CityDiffCallback) {

    /* ViewHolder for a City, takes in the inflated view and the onClick behavior. */
    class CityViewHolder(itemView: View, val onClick: (City) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val cityName: TextView = itemView.findViewById(R.id.cityName)
        private val cityImage: ImageView = itemView.findViewById(R.id.cityImage)

        private var currentCity: City? = null

        init {
            itemView.setOnClickListener {
                println(currentCity)
                currentCity?.let {
                    onClick(it)
                }
            }
        }

        /* Bind table to the respective views */
        fun bind(city: City?) {
            if (city == null) return

            currentCity = city
            cityName.text = city.nameText
            cityImage.setImageResource(city.image)
        }
    }

    /* Creates and inflates view and return TableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_city, parent, false)
        return CityViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = getItem(position)
        holder.bind(city)
    }
}

object CityDiffCallback : DiffUtil.ItemCallback<City>() {
    override fun areItemsTheSame(oldcity: City, newcity: City): Boolean {
        return oldcity == newcity
    }

    override fun areContentsTheSame(oldcity: City, newcity: City): Boolean {
        return oldcity.name == newcity.name
    }
}