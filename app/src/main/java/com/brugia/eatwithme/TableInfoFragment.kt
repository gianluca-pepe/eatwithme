package com.brugia.eatwithme

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.data.Person
import com.brugia.eatwithme.data.mealcategory.MealCategory
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import com.brugia.eatwithme.userlist.PersonsAdapter
import com.bumptech.glide.signature.ObjectKey
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.NonCancellable.cancel

class TableInfoFragment : Fragment() {
    // used for editing
    private val newTableViewModel by activityViewModels<CreateTableViewModel>()
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
    private lateinit var userModalLayout: ConstraintLayout
    private lateinit var btnCloseUserModal: Button
    private lateinit var img_userpic: ImageView
    private lateinit var txt_user_nomecognome: TextView
    private lateinit var txt_user_descrizione: TextView

    private lateinit var btnModifyTable: Chip
    private lateinit var btnDeleteTable: Chip
    private lateinit var btnExitTable: Chip


    private val personsAdapter = PersonsAdapter { person ->
        onPersonClick(person)
    }

    private val personViewModel: MyProfileViewModel = MyProfileViewModel()
    init {
        personViewModel.getCurrentPerson()
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

        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        context,
                        R.raw.map_style
                )
        )

        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, { it ->
            it.restaurant?.geometry?.location?.latLng?.let { location ->
                googleMap.addMarker(MarkerOptions()
                        .position(location)
                        .title(getString(R.string.my_position))
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f))
            }
        })
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
        val mapFragment = childFragmentManager.findFragmentById(R.id.createTableMap) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

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

        txt_user_nomecognome = view.findViewById(R.id.txtusernomecognome)
        txt_user_descrizione = view.findViewById(R.id.txtuserdescrizione)
        img_userpic = view.findViewById(R.id.img_userpic)
        btnCloseUserModal = view.findViewById(R.id.btnCloseUserModal)
        userModalLayout = view.findViewById(R.id.userModalLayout)
        userModalLayout.visibility = GONE


        btnModifyTable = view.findViewById(R.id.btnModifyTable)
        btnExitTable = view.findViewById(R.id.btnExitTable)
        btnDeleteTable = view.findViewById(R.id.btnDeleteTable)

        btnModifyTable.setOnClickListener { modifyTableClicked() }
        btnExitTable.setOnClickListener { exitTableClicked() }
        btnDeleteTable.setOnClickListener { deleteTableClicked() }

        btnCloseUserModal.setOnClickListener { hideModal() }

        userList.adapter = personsAdapter

        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {

                tableHourTextView.text = it.tableHourText()
                tableDateTextView.text = it.tableDateText()
                tableParticipantsTextView.text = "${it.numParticipants} / ${it.maxParticipants}"

                nameTextView.text = it.name
                descriptionTextView.text = it.description

                // set image
                val photo = when (it.getCategory()) {
                    MealCategory.LUNCH -> R.drawable.pranzo
                    MealCategory.DINNER -> R.drawable.cena
                    MealCategory.BREAKFAST -> R.drawable.colazione
                    MealCategory.APERITIF -> R.drawable.cocktail
                    else -> null
                }
                if (photo != null) {
                    tableImageView.setImageResource(photo)
                }

                txt_restaurant_name.text = it.restaurant?.name
                txt_restaurant_address.text = it.restaurant?.formatted_address
                if(it.restaurant?.price_level != null) {
                    txtct_RestaurantPriceLevel.text = getString(R.string.currency_symbol).repeat(it.restaurant?.price_level!!)
                }else{
                    txtct_RestaurantPriceLevel.visibility = GONE
                }
                if (it.restaurant?.rating != null){
                    ratingBar.rating = it.restaurant?.rating!!
                    txtct_RestaurantReviewsCount.text = it.restaurant?.rating.toString() + " / " + it.restaurant?.user_ratings_total.toString()
                }else{
                    ratingBar.visibility = GONE
                    txtct_RestaurantReviewsCount.visibility = GONE
                }

                personsAdapter.tableOwner = it.ownerId.toString()
            }
        })

        //Check if the user partecipate to the table, show partecipants list
        if (tableViewModel.doesUserParticipate()) {
            txt_table_completed.visibility = GONE
            btnJoin.visibility = GONE
            userList.visibility = VISIBLE
            /* If the person participate to the table, populate the recyclerview*/
            /* Persons list management (RecyclerView) */

            tableViewModel.personsList.observe(viewLifecycleOwner, {
                personsAdapter.submitList(it)
            })

            if(tableViewModel.UserIsCreator()){
                btnModifyTable.visibility = VISIBLE
                btnExitTable.visibility = GONE
                btnDeleteTable.visibility = VISIBLE
            }else{
                btnModifyTable.visibility = GONE
                btnExitTable.visibility = VISIBLE
                btnDeleteTable.visibility = GONE
            }

        } else {
            /*Hide all button*/
            btnModifyTable.visibility = GONE
            btnExitTable.visibility = GONE
            btnDeleteTable.visibility = GONE

            //Prevent user joins table if it is full
            if (tableViewModel.doesTableIsFull()) {
                txt_table_completed.visibility = VISIBLE
                btnJoin.visibility = GONE
                userList.visibility = GONE
            } else {
                txt_table_completed.visibility = GONE
                btnJoin.visibility = VISIBLE
                userList.visibility = GONE
                /* If the person don't participate to the table, give him/she the possibility to join*/
                btnJoin.setOnClickListener {
                    personViewModel.myprofileLiveData.value?.let {
                        this.joinTable(view)
                    }
                }
            }
        }
    }

    private fun joinTable(view: View) {
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

    private fun navigateToMyTables() {
        findNavController().navigate(R.id.myTablesFragment)
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
        btnJoin.visibility = GONE
        userList.visibility = VISIBLE

        /*Show quit button*/
        btnExitTable.visibility = VISIBLE
        /* If the person participate to the table, populate the recyclerview*/
        /* Persons list management (RecyclerView) */

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

    private fun onPersonClick(person: Person) {

        txt_user_nomecognome.text = person.name + " " + person.surname
        txt_user_descrizione.text = person.description

        if (person.profile_pic != null) {
            val imgRef = Firebase.storage.reference.child("profile-pic/${person.id}")
            GlideApp.with(this)
                    .load(imgRef)
                    .signature(ObjectKey(System.currentTimeMillis()))
                    .into(img_userpic)
        }

        userModalLayout.visibility = VISIBLE
    }

    private fun hideModal() {
        userModalLayout.visibility = GONE
    }

    private fun modifyTableClicked(){
        tableViewModel.getSelectedTable().value?.let {
            newTableViewModel.setTable(it)
            newTableViewModel.editing = true
            findNavController().navigate(R.id.action_EditTable)
        }
    }

    private fun exitTableClicked(){
        /*User click on exit table*/
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.table_exit_title)
                setMessage(R.string.table_exit_description)
                setPositiveButton( R.string.yes,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked OK button
                            tableViewModel.exitTable()
                            observeExitState()
                        })
                setNegativeButton(R.string.no,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
            }
            // Set other dialog properties

            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    private fun observeExitState() {
        tableViewModel.exitState.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(context, R.string.table_exit_toast, Toast.LENGTH_SHORT).show()
                Handler().postDelayed(Runnable {
                    //anything you want to start after 1.5s
                    navigateToMyTables()
                }, 1500)

            }else {
                Toast.makeText(context, R.string.table_exit_toast_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteTableClicked() {
        /*User click on delete table*/
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.table_delete_title)
                setMessage(R.string.table_delete_description)
                setPositiveButton( R.string.yes,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User clicked OK button
                            tableViewModel.deleteTable()
                            observeDeleteState()
                        })
                setNegativeButton(R.string.no,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
            }
            // Set other dialog properties

            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    private fun observeDeleteState() {
        tableViewModel.deleteState.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(context, R.string.table_delete_toast, Toast.LENGTH_SHORT).show()
                Handler().postDelayed(Runnable {
                    //anything you want to start after 1.5s
                    navigateToMyTables()
                }, 1500)

            }else {
                Toast.makeText(context, R.string.table_delete_toast_error, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

