package com.brugia.eatwithme.myprofile

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brugia.eatwithme.data.Person
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class MyProfileViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val personID: String =  FirebaseAuth.getInstance().currentUser?.uid.toString()

    val myprofileLiveData: MutableLiveData<Person> = MutableLiveData(
        Person(
            id = personID,
            name = null,
            surname = null,
            telephone = null,
            email = FirebaseAuth.getInstance().currentUser?.email,
            birthday = null,
            profile_pic = null,
            preferences = arrayListOf<String>()
        )
    )

    fun createPerson(name:String?, surname:String?, telephone:String?, birthday:String?, profile_pic:String?, preferences: ArrayList<String> = arrayListOf<String>()) {

        myprofileLiveData.value = myprofileLiveData.value?.copy(
                id = personID,
                name = name,
                surname = surname,
                telephone = telephone,
                email = FirebaseAuth.getInstance().currentUser?.email,
                birthday = birthday,
                profile_pic = profile_pic,
                preferences = preferences
        )

        //create the document whose name is the id of the person (from FireBase)
        myprofileLiveData.value?.let {
            db.collection("Users").document(personID).set(it)
        }
    }

    //Function to get the current user..
    fun getCurrentPerson(){

        //Obtain the document whoose id field is = personID
        val docRef = db.collection("Users").document(personID)

        docRef.get()
                .addOnSuccessListener { document ->
                    //document exists?
                    if (document.data != null) {
                        //Load the current document
                        //Log.d(TAG, "${document.id} => ${document.data}")
                        myprofileLiveData.value = myprofileLiveData.value?.copy(
                                id = document.getString("id"),
                                name = document.getString("name"),
                                surname = document.getString("surname"),
                                telephone = document.getString("telephone"),
                                email = document.getString("email"),
                                birthday = document.getString("birthday"),
                                profile_pic = document.getString("profile_pic"),
                                preferences = arrayListOf<String>()
                        )
                    } else {
                        //document not found
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

        /*
        TO DO:
            Manage preferences
        */
    }

    //Update person data on db
    fun updateCurrentPerson(name:String?, surname:String?, telephone:String?, birthday:String?, profile_pic:String? = null, preferences: ArrayList<String> = arrayListOf<String>()){
        //Obtain the document whoose id field is = personID
        val docRef = db.collection("Users").document(personID)

        docRef.get()
                .addOnSuccessListener { document ->
                    //document exists?
                    if (document.data != null) {
                        //Load the current document
                        //Log.d(TAG, "${document.id} => ${document.data}")
                        myprofileLiveData.value = myprofileLiveData.value?.copy(
                                id = personID,
                                name = name,
                                surname = surname,
                                telephone = telephone,
                                email = FirebaseAuth.getInstance().currentUser?.email,
                                birthday = birthday,
                                profile_pic = profile_pic,
                                preferences = arrayListOf<String>()
                        )

                        //Update the document inside DB
                        myprofileLiveData.value?.let {
                            db.collection("Users").document(personID).update(mapOf(
                                    "name" to  it.name,
                                    "surname" to it.surname,
                                    "telephone" to it.telephone,
                                    "email" to it.email,
                                    "birthday" to it.birthday,
                                    "profile_pic" to it.profile_pic
                            ))
                        }

                    } else {
                        //document not found
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

    }

    /*Delete current person data from DB (keep care to logout the person on the end and redirect to the login page..)*/
    fun deleteCurrentPersonData(){
        //Delete the document of the Person, note that this doesn't delete its subdrirectories
        db.collection("Users").document(personID)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

    }

    /*
    * This function check if person data already exists
    * if not, create the person document into Firestore
    * if yes, load the data of the person
    *
    * We can call this functions without arguments or with arguments (ex: we have the data from Facebook or Google login)
    *
    * */
    fun checkPersonData(name:String? = null, surname:String? = null, telephone:String? = null, birthday:String? = null, profile_pic:String? = null, preferences: ArrayList<String> = arrayListOf<String>()){

        println("ID persona: $personID")
        val docRef = db.collection("Users").document(personID)

        docRef.get()
                .addOnSuccessListener { document ->
                    //document exists?
                    //println(document.data)
                    if (document.data != null) {
                        println("La persona è già presente nel DB..")
                        getCurrentPerson()
                    }else{
                        println("Creazione persona in corso..")
                        createPerson(name, surname, telephone, birthday, profile_pic, preferences)
                    }
                }
    }


}
