package com.brugia.eatwithme

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.brugia.eatwithme.userlist.PersonsAdapter

class TableInfoFragment : Fragment() {
    private val tableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }
    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var tableImageView: ImageView

    private lateinit var tableDateTextView: TextView
    private lateinit var tableHourTextView: TextView
    private lateinit var tableParticipantsTextView: TextView
    private lateinit var btnJoin: Button
    private lateinit var userList: RecyclerView

    private lateinit var txt_restaurant_name: TextView
    private lateinit var txt_restaurant_address: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var txtct_RestaurantReviewsCount: TextView
    private lateinit var txtct_RestaurantPriceLevel: TextView
    private lateinit var txt_table_completed: TextView

    private val personViewModel: MyProfileViewModel = MyProfileViewModel()
    init {
        personViewModel.getCurrentPerson()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_info, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView = view.findViewById(R.id.table_name)
        descriptionTextView = view.findViewById(R.id.table_description)
        tableImageView = view.findViewById(R.id.table_image)

        tableDateTextView = view.findViewById(R.id.table_date2)
        tableHourTextView = view.findViewById(R.id.table_hour2)
        tableParticipantsTextView = view.findViewById(R.id.table_num_participants2)

        btnJoin = view.findViewById<Button>(R.id.join_table_button2)
        userList = view.findViewById(R.id.recycler_view_person_list)

        txt_restaurant_name = view.findViewById(R.id.txt_restaurant_name)
        txt_restaurant_address = view.findViewById(R.id.txt_restaurant_address)
        ratingBar = view.findViewById(R.id.ratingBar2)
        txtct_RestaurantReviewsCount = view.findViewById(R.id.txtct_RestaurantReviewsCount2)
        txtct_RestaurantPriceLevel = view.findViewById(R.id.txtct_RestaurantPriceLevel2)

        txt_table_completed = view.findViewById(R.id.txt_table_completed)

        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {

                tableHourTextView.text = it.tableHourText()
                tableDateTextView.text = it.tableDateText()
                tableParticipantsTextView.text = "${it.numParticipants} / ${it.maxParticipants}"

                nameTextView.text = it.name
                descriptionTextView.text = it.description
                // set image
                val hours = it.tableHourText()
                //Check the hour and set the image according it
                if (hours >= "05:00" && hours < "11:30") {
                    tableImageView.setImageResource(R.drawable.colazione)
                } else if (hours >= "11:30" && hours < "15:00") {
                    tableImageView.setImageResource(R.drawable.pranzo)
                } else if (hours >= "19:00" && hours < "22:30") {
                    tableImageView.setImageResource(R.drawable.cena)
                } else {
                    tableImageView.setImageResource(R.drawable.cocktail)//in every other hours, just a cocktail..
                }

                txt_restaurant_name.text = it.restaurant?.name
                txt_restaurant_address.text = it.restaurant?.formatted_address
                if(it.restaurant?.price_level != null) {
                    txtct_RestaurantPriceLevel.text = getString(R.string.currency_symbol).repeat(it.restaurant?.price_level!!)
                }else{
                    txtct_RestaurantPriceLevel.visibility = INVISIBLE
                }
                if (it.restaurant?.rating != null){
                    ratingBar.rating = it.restaurant?.rating!!
                    txtct_RestaurantReviewsCount.text = it.restaurant?.rating.toString() + " / " + it.restaurant?.user_ratings_total.toString()
                }else{
                    ratingBar.visibility = INVISIBLE
                    txtct_RestaurantReviewsCount.visibility = INVISIBLE
                }
            }
        })

        //Check if the user partecipate to the table, show partecipants list
        if (tableViewModel.doesUserParticipate()) {
            txt_table_completed.visibility = INVISIBLE
            btnJoin.visibility = INVISIBLE
            userList.visibility = VISIBLE
            /* If the person participate to the table, populate the recyclerview*/
            /* Persons list management (RecyclerView) */
            val personsAdapter = PersonsAdapter()
            userList.adapter = personsAdapter

            tableViewModel.personsList.observe(viewLifecycleOwner, {
                personsAdapter.submitList(it)
            })

        } else {
            //Prevent user joins table if it is full
            if (tableViewModel.doesTableIsFull()) {
                txt_table_completed.visibility = VISIBLE
                btnJoin.visibility = INVISIBLE
                userList.visibility = INVISIBLE
            } else {
                txt_table_completed.visibility = INVISIBLE
                btnJoin.visibility = VISIBLE
                userList.visibility = INVISIBLE
                /* If the person don't participate to the table, give him/she the possibility to join*/
                btnJoin.setOnClickListener {
                    personViewModel.myprofileLiveData.value?.let {
                        this.joinTable(view)
                    }
                }
            }
        }
    }

    fun joinTable(view: View) {
        val person = personViewModel.myprofileLiveData.value
        if (person == null) return

        if ( person.isProfileIncomplete() ) {
            val alert = AlertDialog.Builder(this.requireContext())
            alert
                    .setTitle(R.string.incomplete_account_title)
                    .setMessage(R.string.incomplete_account_message)
                    .setNegativeButton(R.string.not_now) { dialog, which ->
                        /* user refuse to update profile, return to main fragment */
                        navigateToMain()
                    }
                    .setPositiveButton(R.string.incomplete_account_pos_button) { dialog, which ->
                        // user accept to update profile, go to profile settings fragment
                        findNavController().navigate(R.id.myProfileSettingsFragment)
                    }
                    .create().show()
        } else {
            tableViewModel.joinTable()
            observeJoinState()
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.homepageFragment)
    }

    private fun observeJoinState() {
        tableViewModel.joinState.observe(viewLifecycleOwner, {
            if (it) {
                updateTableData()
            }else {
                showFailureDialog()
                navigateToMain()
            }
        })
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(this.requireContext())
                .setTitle(R.string.incomplete_account_title)
                .setMessage(R.string.incomplete_account_message)
                .setNegativeButton(R.string.not_now) { dialog, which ->
                    /* user refuse to update profile, return to main fragment */
                    this.navigateToMain()
                }
                .setPositiveButton(R.string.incomplete_account_pos_button) { dialog, which ->
                    // user accept to update profile, go to profile settings fragment
                    findNavController().navigate(R.id.myProfileSettingsFragment)
                }
                .create().show()
    }

    private fun updateTableData(){
        btnJoin.visibility = INVISIBLE
        userList.visibility = VISIBLE
        /* If the person participate to the table, populate the recyclerview*/
        /* Persons list management (RecyclerView) */
        val personsAdapter = PersonsAdapter()
        userList.adapter = personsAdapter

        tableViewModel.personsList.observe(viewLifecycleOwner, {
            personsAdapter.submitList(it)
        })

        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {
                tableParticipantsTextView.text = "${it.numParticipants + 1} / ${it.maxParticipants}"
                Toast.makeText(context, "Ti sei unito al tavolo", Toast.LENGTH_SHORT).show()
            }
        })
    }

}