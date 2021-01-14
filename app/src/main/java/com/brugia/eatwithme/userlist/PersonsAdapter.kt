package com.brugia.eatwithme.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.GlideApp
import com.brugia.eatwithme.R
import com.brugia.eatwithme.data.Person
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class PersonsAdapter(): ListAdapter<Person, PersonsAdapter.PersonViewHolder>(PersonDiffCallback) {

    /* ViewHolder for Person, takes in the inflated view and the onClick behavior. */
    class PersonViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val personNameTextView: TextView = itemView.findViewById(R.id.person_list_name)
        private val personSurnameTextView: TextView = itemView.findViewById(R.id.person_list_surname)
        private val personImageView: ImageView  = itemView.findViewById(R.id.person_list_img)

        /* Bind person to the respective views */
        fun bind(person: Person) {
            personNameTextView.text = person.name
            personSurnameTextView.text = person.surname

            if (person.profile_pic != null) {
                println(person.profile_pic)
                val imgRef = Firebase.storage.reference.child("profile-pic/${person.id}")
                GlideApp.with(itemView)
                        .load(imgRef)
                        .into(personImageView)
            }

            //tableTextViewDate.text = table.tableDateText()
            //tableTextViewHour.text = table.tableHour()

            //if (table.image != null) {
            //tableImageView.setImageResource(table.image)
            //} else {
            //tableImageView.setImageResource(R.drawable.logo_login)//here we can put a default table image if missing
            //}
        }
    }

    /* Creates and inflates view and return PersonViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.person_row_item, parent, false)
        println("ok")
        return PersonViewHolder(view)
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