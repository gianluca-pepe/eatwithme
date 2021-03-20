package com.brugia.eatwithme.myprofile

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.brugia.eatwithme.data.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class MyProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    private val personID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val personNameSurname: String =  FirebaseAuth.getInstance().currentUser?.displayName.toString()

    val myprofileLiveData: MutableLiveData<Person> = MutableLiveData(
        Person(
            id = personID,
            name = null,
            surname = null,
            telephone = null,
            description = null,
            email = FirebaseAuth.getInstance().currentUser?.email,
            birthday = null,
            profile_pic = null,
            preferences = arrayListOf<String>()
        )
    )

    // Instantiate the RequestQueue.
    val queue = Volley.newRequestQueue(application)
    private val url = "https://sapienzaengineering.eu.pythonanywhere.com/api/v1.0/users"

    fun createPerson(name:String?, surname:String?, telephone:String? = null, birthday:String? = null, profile_pic:String? = null, preferences: ArrayList<String> = arrayListOf<String>(), description: String? = null) {

        myprofileLiveData.value = myprofileLiveData.value?.copy(
                id = personID,
                name = name,
                surname = surname,
                telephone = telephone,
                description = description,
                email = FirebaseAuth.getInstance().currentUser?.email,
                birthday = birthday,
                profile_pic = profile_pic,
                preferences = preferences
        )

        val bodyJSON = JSONObject()
        bodyJSON.put("gid", personID)
        bodyJSON.put( "name", name)
        bodyJSON.put( "surname", surname)
        bodyJSON.put( "telephone", telephone)
        bodyJSON.put("description", description)
        //bodyJSON["birthday"] = birthday
        bodyJSON.put("profile_pic", profile_pic)


        // Request a string response from the provided URL.
        val stringRequest = JsonObjectRequest(Request.Method.POST, url, bodyJSON,
                { response ->
                    println(response)
                    setResponseInLiveData(response.toString())
                },
                { error -> println(error.networkResponse.statusCode) }
        )
        queue.add(stringRequest)
    }

    //Function to get the current user..
    fun getCurrentPerson(){
        val getUserUrl = "$url/$personID"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, getUserUrl,
                { response ->
                    println(response)
                    setResponseInLiveData(response)
                },
                { error -> println(error.networkResponse.statusCode) }
        )
        queue.add(stringRequest)
        /*
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
                                description = document.getString("description"),
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
*/
        /*
        TO DO:
            Manage preferences
        */
    }

    //Update person data on db
    fun updateCurrentPerson(name:String?, surname:String?, telephone:String?, description:String?, birthday:String?, preferences: ArrayList<String> = arrayListOf<String>()){
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
                                description = description,
                                email = FirebaseAuth.getInstance().currentUser?.email,
                                birthday = birthday,
                                preferences = arrayListOf<String>()
                        )

                        //Update the document inside DB
                        myprofileLiveData.value?.let {
                            db.collection("Users").document(personID).update(mapOf(
                                    "name" to  it.name,
                                    "surname" to it.surname,
                                    "telephone" to it.telephone,
                                    "description" to it.description,
                                    "email" to it.email,
                                    "birthday" to it.birthday
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

    fun updatePersonPic(profile_pic: String?){
        println("Aggiornamento immagine profilo della persona")
        //Obtain the document whoose id field is = personID
        val docRef = db.collection("Users").document(personID)

        docRef.get()
            .addOnSuccessListener { document ->
                //document exists?
                if (document.data != null) {
                    //Load the current document
                    //Log.d(TAG, "${document.id} => ${document.data}")
                    myprofileLiveData.value = myprofileLiveData.value?.copy(
                        profile_pic = profile_pic
                    )

                    //Update the document inside DB
                    myprofileLiveData.value?.let {
                        db.collection("Users").document(personID).update(mapOf(
                            "profile_pic" to  it.profile_pic
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
    fun checkPersonData(name:String? = null, surname:String? = null, telephone:String? = null, birthday:String? = null, profile_pic:String? = null, preferences: ArrayList<String> = arrayListOf<String>(), description: String? = null){

        println("ID persona: $personID")
        /*
        val docRef = db.collection("Users").document(personID)

        docRef.get()
                .addOnSuccessListener { document ->
                    //document exists?
                    //println(document.data)
                    if (document.data != null) {
                        println("La persona è già presente nel DB..")
                        getCurrentPerson()
                    }else{
                        val n_s = personNameSurname.split(" ")
                        val nome = n_s[0]
                        val cognome = n_s[1]
                        println("Creazione persona in corso..")
                        createPerson(nome, cognome, telephone, birthday, profile_pic, preferences, description)
                    }
                }
*/
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, "$url/$personID",
            { response ->
                println("La persona è già presente nel DB..")
                //setResponseInLiveData(response)
                getCurrentPerson()
            },
            { error ->
                if (error.networkResponse.statusCode == 404) {
                    val n_s = personNameSurname.split(" ")
                    val nome = n_s[0]
                    val cognome = n_s[1]
                    println("Creazione persona in corso..")
                    createPerson(nome,cognome)
                }
            }
        )

        queue.add(stringRequest)
    }

    private fun setResponseInLiveData(response: String) {
        val result = JSONObject(response)
        // GESTIRE VALORI NULL
        myprofileLiveData.value = myprofileLiveData.value?.copy(
                id = result.optString("id"),
                name = result.optString("name"),
                surname = result.optString("surname"),
                telephone = result.optString("telephone"),
                description = result.optString("description"),
                email = result.optString("email"),
                birthday = result.optString("birthday"),
                profile_pic = result.optString("profile_pic"),
                //preferences = arrayListOf<String>()
        )
    }
}

class MyProfileViewModelFactory(private val context: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyProfileViewModel(
                    application = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
