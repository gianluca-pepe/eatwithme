/*
*  For image upload details see: https://adorahack.com/upload-gallery-image-to-firebase-from-android-app-kotlin
   To display image https://firebase.google.com/docs/storage/android/download-files?authuser=0#downloading_images_with_firebaseui (error)
   *see this: https://medium.com/@egemenhamutcu/displaying-images-from-firebase-storage-using-glide-for-kotlin-projects-3e4950f6c103
 */


package com.brugia.eatwithme

import android.R.string
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.brugia.eatwithme.datetimepickers.DatePickerFragment
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.brugia.eatwithme.myprofile.MyProfileViewModelFactory
import com.bumptech.glide.signature.ObjectKey

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MyProfileFragment : Fragment() {


    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    //private val storage = Firebase.storage
    private val db = Firebase.firestore
    private val personID: String =  FirebaseAuth.getInstance().currentUser?.uid.toString()

    private val calendar: Calendar = Calendar.getInstance()
    private val personViewModel: MyProfileViewModel by viewModels {
        MyProfileViewModelFactory(requireActivity().application)
    }

    private lateinit var pers_name: TextInputEditText
    private lateinit var pers_surname: TextInputEditText
    private lateinit var pers_birthday: TextView
    private lateinit var pers_birthday_calendar: ImageView
    private lateinit var pers_telephone: TextInputEditText
    private lateinit var pers_description: TextInputEditText
    private lateinit var pers_email: TextInputEditText
    private lateinit var modifyProfile: Button
    private lateinit var btn_choose_image: Button
    private lateinit var btn_upload_image: Button
    private lateinit var img_userpic: ImageView
    private lateinit var btn_logout: Button

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
        pers_description = view.findViewById(R.id.input_user_description)

        modifyProfile = view.findViewById(R.id.btn_modify_profile)
        //btn_choose_image = view.findViewById(R.id.btn_choose_image)
        btn_upload_image = view.findViewById(R.id.btn_upload_image)
        img_userpic = view.findViewById(R.id.img_userpic)

        btn_logout = view.findViewById(R.id.btn_user_logout)

        personViewModel.checkPersonData()//check if person data are loaded and load them


        // Reference to an image file in Cloud Storage
        var imageref = Firebase.storage.reference.child("profile-pic/$personID")
        //Fill variables from DB
        personViewModel.myprofileLiveData.observe(viewLifecycleOwner, {
            pers_name.setText(it.name)
            pers_surname.setText(it.surname)
            if(it.birthday != null) {
                val str = it.birthday!!.split("/")
                val month = str[1]
                val day = str[2]
                val year = str[0]
                pers_birthday.text = "$day/$month/$year"
            }else{
                pers_birthday.text = ""
            }
            pers_telephone.setText(it.telephone)
            pers_description.setText(it.description)
            if(it.profile_pic != null){

                //val storage = FirebaseStorage.getInstance()
                // Create a reference to a file from a Google Cloud Storage URI
                //val gsReference = storage.getReferenceFromUrl("gs://bucket/images/stars.jpg")
                GlideApp.with(this)
                        .load(imageref)
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .into(img_userpic)

            }
        })

        pers_birthday_calendar.setOnClickListener { this.showDatePickerDialog() }
        modifyProfile.setOnClickListener { this.modifyProfile() }
        btn_logout.setOnClickListener { this.logout() }

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        //btn_choose_image.setOnClickListener { launchGallery() }
        btn_upload_image.setOnClickListener { launchGallery() }


        return view
    }

    fun modifyProfile(){
        var ok: Boolean = true

        //Update user info
        if(pers_name.text.toString() == ""){
            ok = false
            pers_name.error = "Inserisci il tuo nome"
        }
        if(pers_surname.text.toString() == ""){
            ok = false
            pers_surname.error = "Inserisci il tuo cognome"
        }
        if(pers_birthday.text.toString() == ""){
            ok = false
            pers_birthday.error = "Inserisci la tua data di nascita"
        }
        val str = pers_birthday.text.split("/")
        val month = str[1]
        val day = str[0]
        val year = str[2]
        val dateOfBirth = "$year/$month/$day"
        if(ok){

            //val datebirth = SimpleDateFormat("yyyy/MM/dd").parse(pers_birthday.text.toString())
            personViewModel.updateCurrentPerson(
                    name = pers_name.text.toString(),
                    surname = pers_surname.text.toString(),
                    telephone = pers_telephone.text.toString(),
                    birthday = dateOfBirth,
                    description = pers_description.text?.toString()
            )

            Toast.makeText(context, "Profile info succesfully modified", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog() {
        datePicker.show(this.requireActivity().supportFragmentManager, "datePicker")
    }

    private fun onDateSet(year: Int, month: Int, day: Int) {

        var dateString: String = "$day"

        if(day>9){
            dateString = "$day"
        }else{
            dateString = "0$day"
        }

        if(month>9){
            dateString = "$dateString/$month"
        }else{
            dateString = "$dateString/0$month"
        }

        dateString = "$dateString/$year"

        pers_birthday.setText(dateString)

    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    private fun uploadImage(){
        if(filePath != null){
            //val ref = storageReference?.child("profile-pic/" + UUID.randomUUID().toString())
            val ref = storageReference?.child("profile-pic/$personID")
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    // Add image record to db
                    personViewModel.updatePersonPic(downloadUri.toString())

                    Toast.makeText(context, "Profile pic was successfully modified", Toast.LENGTH_SHORT).show()

                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            Toast.makeText(context, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                /*
                //println(filePath)
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                //println(bitmap)
                img_userpic.setImageBitmap(bitmap)
                */
                uploadImage()


            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun logout(){
        Firebase.auth.signOut()
        startActivity(Intent(context, LoginRegisterActivity::class.java))
        activity?.finish()
    }


}