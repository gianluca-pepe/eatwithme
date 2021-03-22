package com.brugia.eatwithme.data.user

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.brugia.eatwithme.data.ParticipantsListLiveData
import com.brugia.eatwithme.data.Person
import com.brugia.eatwithme.data.Table
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

class UserRepository(application: Application) {
    private val mUser = FirebaseAuth.getInstance().currentUser
    private val personNameSurname: String =  mUser?.displayName.toString()
    private var personTkn: String = ""
    // Instantiate the RequestQueue.
    val queue: RequestQueue = Volley.newRequestQueue(application)
    private val url = "https://sapienzaengineering.eu.pythonanywhere.com/api/v1.0/users"

    private val _currentPersonLiveData: MutableLiveData<Person> = MutableLiveData<Person>()
    val currentPersonLiveData: LiveData<Person>
        get() = _currentPersonLiveData

    fun getUser(token: String): MutableLiveData<Person> {
        val result = MutableLiveData<Person>()

        val getUserUrl = "$url/$token"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, getUserUrl,
                { response ->
                    println(response)
                    result.value = getPersonFromResponse(response)
                },
                { error ->
                    println(error.networkResponse.statusCode)
                }
        )
        queue.add(stringRequest)

        return result
    }

    fun getCurrentUser() {
        val task = fun () {
            val getUserUrl = "$url/$personTkn"
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(Request.Method.GET, getUserUrl,
                    { response ->
                        println(response)
                        _currentPersonLiveData.value = getPersonFromResponse(response)
                    },
                    { error ->
                        println(error.networkResponse.statusCode)
                    }
            )
            queue.add(stringRequest)
        }

        performRequest(task)
    }

    /*
    * This function check if person data already exists
    * if not, create the person document into Firestore
    * if yes, load the data of the person
    *
    * We can call this functions without arguments or with arguments (ex: we have the data from Facebook or Google login)
    *
    * */
    fun checkPersonData() {
        val task = fun() {
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(Request.Method.GET, "$url/$personTkn",
                    { response ->
                        println("La persona è già presente nel DB..")
                        //setResponseInLiveData(response)
                        //getCurrentPerson()
                    },
                    { error ->
                        if (error.networkResponse.statusCode == 404) {
                            val n_s = personNameSurname.split(" ")
                            val nome = n_s[0]
                            val cognome = n_s[1]
                            println("Creazione persona in corso..")
                            createPerson(nome, cognome)
                        }
                    }
            )
            queue.add(stringRequest)
        }

        performRequest(task)
    }

    private fun createPerson(name: String?, surname: String?) {

        val bodyJSON = JSONObject()
        bodyJSON.put("gid", personTkn)
        bodyJSON.put("name", name)
        bodyJSON.put("surname", surname)

        val task = fun () {
            // Request a string response from the provided URL.
            val stringRequest = JsonObjectRequest(Request.Method.POST, url, bodyJSON,
                    { response ->
                        println(response)
                        _currentPersonLiveData.value = getPersonFromResponse(response.toString())
                    },
                    { error -> println(error.networkResponse.statusCode) }
            )
            queue.add(stringRequest)
        }

        performRequest(task)
    }

    //Update person data on db
    fun updateCurrentPerson(
            name: String?,
            surname: String?,
            description: String?,
            birthday: String?) {

        val bodyJSON = JSONObject()
        bodyJSON.put("name", name)
        bodyJSON.put("surname", surname)
        bodyJSON.put("description", description)
        bodyJSON.put("birthday", birthday)

        val request = fun() {
            // Request a string response from the provided URL.
            val stringRequest = JsonObjectRequest(Request.Method.PUT, "$url/$personTkn", bodyJSON,
                    { response ->
                        println(response)
                        _currentPersonLiveData.value = getPersonFromResponse(response.toString())
                    },
                    { error -> println(error.networkResponse.statusCode) }
            )
            queue.add(stringRequest)
        }
        performRequest(request)
    }

    fun updateCurrentPersonPic(profile_pic: String?) {
        println("Aggiornamento immagine profilo della persona")

        val bodyJSON = JSONObject()
        bodyJSON.put("profile_pic", profile_pic)

        val request = fun() {
            // Request a string response from the provided URL.
            val stringRequest = JsonObjectRequest(Request.Method.PUT, "$url/$personTkn", bodyJSON,
                    { response ->
                        println(response)
                        _currentPersonLiveData.value = getPersonFromResponse(response.toString())
                    },
                    { error -> println(error.networkResponse.statusCode) }
            )
            queue.add(stringRequest)
        }

        performRequest(request)
    }

    /*Delete current person data from DB (keep care to logout the person on the end and redirect to the login page..)*/
    fun deleteCurrentPersonData(){
        println("Eliminazione dati profilo della persona dal DB")
        //Delete the document of the Person, note that this doesn't delete its subdrirectories

        val request = fun() {
            val stringRequest = StringRequest(Request.Method.DELETE, "$url/$personTkn",
                    { response ->
                        println(response)
                        //setResponseInLiveData(response.toString())
                    },
                    { error -> println(error.networkResponse.statusCode) }
            )
            queue.add(stringRequest)
        }

        performRequest(request)
        _currentPersonLiveData.value = Person() // empty person, allow UI to update
    }

     /**
     *  task is basically a request needing the idToken for authentication on backend.
     */
    private fun performRequest( request: () -> Unit) {
        if ( personTkn.isNullOrEmpty() ) {
            mUser!!.getIdToken(true).addOnCompleteListener { getIdTokenTask ->
                if (getIdTokenTask.isSuccessful) {
                    personTkn = getIdTokenTask.result.token.toString()
                    // println("token:" + personTkn)
                    request()
                } else {
                    // Handle error -> task.getException();
                }
            }
        } else {
            println("token già presente")
            request()
        }
    }

    private fun getPersonFromResponse(response: String): Person {
        val result = JSONObject(response)

        return Person (
                id = result.optString("gid"),
                name = result.optString("name"),
                surname = result.optString("surname"),
                description = result.optString("description"),
                birthday = result.optString("birthday"),
                profile_pic = result.optString("profile_pic")
        )
    }
}