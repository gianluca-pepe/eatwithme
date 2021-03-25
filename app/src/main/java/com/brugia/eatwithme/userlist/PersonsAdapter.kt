package com.brugia.eatwithme.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.GlideApp
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Person
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.chip.Chip

class PersonsAdapter(private val onClick: (Person) -> Unit): ListAdapter<Person, PersonsAdapter.PersonViewHolder>(PersonDiffCallback) {

    var tableOwner: String = ""

    /* ViewHolder for Person, takes in the inflated view and the onClick behavior. */
    class PersonViewHolder(itemView: View, tableOwner: String, val onClick: (Person) -> Unit): RecyclerView.ViewHolder(itemView) {

        private val personNameTextView: TextView = itemView.findViewById(R.id.person_list_name)
        private val personSurnameTextView: TextView = itemView.findViewById(R.id.person_list_surname)
        private val personImageView: ImageView  = itemView.findViewById(R.id.person_list_img)
        private val lblTableOwner: Chip = itemView.findViewById(R.id.lblTableOwner)
        private var currentPerson: Person? = null
        var tableOwner: String = tableOwner


        init {
            itemView.findViewById<LinearLayout>(R.id.person_row).setOnClickListener {
                currentPerson?.let {
                    onClick(it)
                }
            }
        }

        /* Bind person to the respective views */
        fun bind(person: Person) {
            println("person id:" + person.id)
            currentPerson = person
            personNameTextView.text = person.name
            personSurnameTextView.text = person.surname
            lblTableOwner.visibility = INVISIBLE

            if (person.profile_pic != null) {
                GlideApp.with(itemView)
                        .load(person.profile_pic)
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .into(personImageView)
            }

            if( person.isOwner != null && person.isOwner == true ){
                lblTableOwner.visibility = VISIBLE
            }

        }
    }

    /* Creates and inflates view and return PersonViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.person_row_item, parent, false)
        println("ok")
        return PersonViewHolder(view, tableOwner, onClick)
    }

    /* Gets current person and uses it to bind view. */
    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = getItem(position)
        holder.bind(person)

    }
}

object PersonDiffCallback : DiffUtil.ItemCallback<Person>() {
    override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem.id == newItem.id
    }
}