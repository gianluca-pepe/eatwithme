package com.brugia.eatwithme

import android.R.string
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brugia.eatwithme.datetimepickers.DatePickerFragment
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*


class MyProfileFragment : Fragment() {

    private val calendar: Calendar = Calendar.getInstance()
    private val personViewModel: MyProfileViewModel = MyProfileViewModel()

    private lateinit var pers_name: TextInputEditText
    private lateinit var pers_surname: TextInputEditText
    private lateinit var pers_birthday: TextView
    private lateinit var pers_birthday_calendar: ImageView
    private lateinit var pers_telephone: TextInputEditText
    private lateinit var pers_email: TextInputEditText
    private lateinit var modifyProfile: Button

    private val datePicker = DatePickerFragment(::onDateSet)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        pers_name = view.findViewById(R.id.input_user_name)
        pers_surname = view.findViewById(R.id.input_user_surname)
        pers_birthday = view.findViewById(R.id.input_user_birthday)
        pers_birthday_calendar = view.findViewById(R.id.img_user_calendar)
        //pers_email = view.findViewById(R.id.input_user_mail)
        pers_telephone = view.findViewById(R.id.input_user_telephone)
        modifyProfile = view.findViewById(R.id.btn_modify_profile)

        personViewModel.checkPersonData()//check if person data are loaded and load them

        //Fill variables from DB
        personViewModel.myprofileLiveData.observe(viewLifecycleOwner, {
            pers_name.setText(it.name)
            pers_surname.setText(it.surname)
            pers_birthday.text = it.birthday?.toString() ?: ""
            pers_telephone.setText(it.telephone)
        })

        pers_birthday_calendar.setOnClickListener { this.showDatePickerDialog() }
        modifyProfile.setOnClickListener { this.modifyProfile() }

        return view
    }

    fun modifyProfile(){
        var ok: Boolean = true

        //Update user info
        if(pers_name.text.toString() == ""){
            ok = false
            Toast.makeText(context, "Insert your name", Toast.LENGTH_SHORT).show()
        }
        if(pers_surname.text.toString() == ""){
            ok = false
            Toast.makeText(context, "Insert your surname", Toast.LENGTH_SHORT).show()
        }
        if(pers_birthday.text.toString() == ""){
            ok = false
            Toast.makeText(context, "Insert your birthday date", Toast.LENGTH_SHORT).show()
        }
        if(pers_telephone.text.toString() == ""){
            ok = false
            Toast.makeText(context, "Insert your telephone", Toast.LENGTH_SHORT).show()
        }

        if(ok){

            //val datebirth = SimpleDateFormat("yyyy/MM/dd").parse(pers_birthday.text.toString())
            personViewModel.updateCurrentPerson(
                    name = pers_name.text.toString(),
                    surname = pers_surname.text.toString(),
                    telephone = pers_telephone.text.toString(),
                    birthday = pers_birthday.text.toString()
            )

            Toast.makeText(context, "Profile info succesfully modified", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog() {
        datePicker.show(this.requireActivity().supportFragmentManager, "datePicker")
    }

    private fun onDateSet(year: Int, month: Int, day: Int) {
        var dateString: String = "$year"
        if(month>9){
            dateString = "$dateString/$month"
        }else{
            dateString = "$dateString/0$month"
        }

        if(day>9){
            dateString = "$dateString/$day"
        }else{
            dateString = "$dateString/0$day"
        }
        pers_birthday.setText(dateString)

    }
}