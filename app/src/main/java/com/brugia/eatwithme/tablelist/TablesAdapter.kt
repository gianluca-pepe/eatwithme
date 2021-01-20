
package com.brugia.eatwithme.tablelist


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Table
import com.google.firebase.auth.FirebaseAuth
import kotlin.reflect.typeOf

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
        private val tableTextViewTitle: TextView = itemView.findViewById(R.id.table_list_title)
        private val tableTextViewPartecipants: TextView = itemView.findViewById(R.id.table_list_lbl_num_partecipants)
        private val tableTextViewDate: TextView = itemView.findViewById(R.id.table_list_lbl_date)
        private val tableTextViewHour: TextView = itemView.findViewById(R.id.table_list_lbl_hour)
        private val tableImageView: ImageView = itemView.findViewById(R.id.table_list_img)
        private var currentTable: Table? = null
        private val tableTextViewOwner: TextView = itemView.findViewById(R.id.table_list_owner)

        init {
            itemView.findViewById<Button>(R.id.table_list_btn_view).setOnClickListener {
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
            tableTextViewPartecipants.text = table.numParticipants.toString() + "/" + table.maxParticipants.toString()
            tableTextViewDate.text = table.tableDateText()
            tableTextViewHour.text = table.tableHour()
            if (table.image != null) {
                tableImageView.setImageResource(table.image!!)
            } else {
                tableImageView.setImageResource(R.drawable.logo_login)//here we can put a default table image if missing
            }

            //Add owner label
            if(personID == table.ownerId){
                tableTextViewOwner.text = "Owner"
            }else{
                tableTextViewOwner.text = ""
            }
        }
    }

    class LoadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), ItemViewHolder {
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)

        override fun bind(item: Table?) {
        }
    }

    /**
     * Set the type: if item is null then assign the loadingViewHolder type
     */
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null)
            VIEW_TYPE_LOADING
        else
            VIEW_TYPE_ITEM
    }

    /* Creates and inflates view and return TableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.table_row_item, parent, false)
            TableViewHolder(view, onClick)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
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