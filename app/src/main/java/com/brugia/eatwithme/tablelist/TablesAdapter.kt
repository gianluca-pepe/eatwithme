
package com.brugia.eatwithme.tablelist


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Table
import com.google.firebase.auth.FirebaseAuth

val personID: String =  FirebaseAuth.getInstance().currentUser?.uid.toString()

class TablesAdapter(private val onClick: (Table) -> Unit) :
        ListAdapter<Table, TablesAdapter.TableViewHolder>(TableDiffCallback) {



    /* ViewHolder for Table, takes in the inflated view and the onClick behavior. */
    class TableViewHolder(itemView: View, val onClick: (Table) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
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
        fun bind(table: Table) {
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

    /* Creates and inflates view and return TableViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.table_row_item, parent, false)
        return TableViewHolder(view, onClick)
    }

    /* Gets current table and uses it to bind view. */
    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val table = getItem(position)
        holder.bind(table)

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