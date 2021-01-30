package com.brugia.eatwithme.homepage

import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.data.mealcategory.MealCategory
import com.brugia.eatwithme.location.LocationViewModel
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import java.util.*


val personID: String =  FirebaseAuth.getInstance().currentUser?.uid.toString()

class TablesAdapter(private val onClick: (Table) -> Unit) :
        ListAdapter<Table, RecyclerView.ViewHolder>(TableDiffCallback) {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    /**
     * Two types of view holders:
     * - one for tables (receives a table)
     * - one for progress bar at the bottom (receives null)
     *
     * this interface is needed in order to have a generic "type" of
     * view holder so we can call .bind() in 'onBindViewHolder'
     * no matter the type of the view holder we need.
     */
    interface ItemViewHolder {
        fun bind(item: Table?) {}
    }

    /* ViewHolder for Table, takes in the inflated view and the onClick behavior. */
    class TableViewHolder(itemView: View, val onClick: (Table) -> Unit) :
            RecyclerView.ViewHolder(itemView), ItemViewHolder {
        private val tableTextViewTitle: TextView = itemView.findViewById(R.id.tableTitle)
        private val tableTextViewSubtitle: TextView = itemView.findViewById(R.id.tableSubtitle)
        private val tableTextViewNumPartecipants: TextView = itemView.findViewById(R.id.numParticipants)
        private val tableTextViewMaxPartecipants: TextView = itemView.findViewById(R.id.maxParticipants)
        //private val tableTextViewDate: TextView = itemView.findViewById(R.id.table_list_lbl_date)
        private val tableTextViewHour: TextView = itemView.findViewById(R.id.tableHour)
        private val tableImageView: ImageView = itemView.findViewById(R.id.tableImage)
        private val tableTextViewCity: TextView = itemView.findViewById(R.id.tableCity)
        private var currentTable: Table? = null
        private val tableTextViewOwner: Chip = itemView.findViewById(R.id.tableOwner)

        init {
            itemView.setOnClickListener {
                currentTable?.let {
                    onClick(it)
                }
            }
        }

        /* Bind table to the respective views */
        override fun bind(table: Table?) {

            if (table == null) return
            currentTable = table
            tableTextViewTitle.text = table.name
            tableTextViewSubtitle.text = table.description
            tableTextViewNumPartecipants.text = table.numParticipants.toString()
            tableTextViewMaxPartecipants.text = table.maxParticipants.toString()
            tableTextViewHour.text = table.tableHourText()

            val lat = table.restaurant?.geometry?.location?.lat!!
            val lng = table.restaurant?.geometry?.location?.lng!!
            tableTextViewCity.text = LocationViewModel.getCityName(lat, lng, itemView.context)


            /*
            if (table.image != null) {
                tableImageView.setImageResource(table.image!!)
            } else {
                tableImageView.setImageResource(R.drawable.logo_login)
            }
            */
            val photo = when (table.getCategory()) {
                MealCategory.LUNCH -> R.drawable.pranzo
                MealCategory.DINNER -> R.drawable.cena
                MealCategory.BREAKFAST -> R.drawable.colazione
                MealCategory.APERITIF -> R.drawable.cocktail
                else -> null
            }

            if (photo != null) tableImageView.setImageResource(photo)

            //Add owner label
            if(personID != table.ownerId){
                tableTextViewOwner.visibility = View.GONE
            }
        }
    }

    class LoadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), ItemViewHolder {
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)

        override fun bind(item: Table?) {
        }
    }

    /* Creates and inflates view and return TableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_table, parent, false)
        return TableViewHolder(view, onClick)
    }

    /* Gets current table and uses it to bind view. */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val table = getItem(position)
        (holder as ItemViewHolder).bind(table)
    }
}

object TableDiffCallback : DiffUtil.ItemCallback<Table>() {
    override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean {
        return oldItem.id == newItem.id
    }
}