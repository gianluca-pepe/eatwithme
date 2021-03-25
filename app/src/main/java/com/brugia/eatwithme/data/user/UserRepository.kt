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
    private val urlUser = "https://sapienzaengineering.eu.pythonanywhere.com/api/v1.0/users"
    private val urlTable = "https://sapienzaengineering.eu.pythonanywhere.com/api/v1.0/table"

    private val _currentPersonLiveData = MutableLiveData<Person>()
    val currentPersonLiveData: LiveData<Person>
        get() = _currentPersonLiveData

    fun getUser(token: String): MutableLiveData<Person> {
        val result = MutableLiveData<Person>()

        val getUserUrl = "$urlUser/$token"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, getUserUrl,
                { response ->
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
            val getUserUrl = "$urlUser/$personTkn"
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(Request.Method.GET, getUserUrl,
                    { response ->
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
            println(personTkn)
            // Request a string response from the provided URL.
            val stringRequest = StringRequest(Request.Method.GET, "$urlUser/$personTkn",
                    { response ->
                        println("La persona è già presente nel DB..")
                        _currentPersonLiveData.value = getPersonFromResponse(response)
                    },
                    { error ->
                        if (error.networkResponse.statusCode == 404) {
                            val n_s = personNameSurname.split(" ")
                            val nome = n_s[0]
                            val cognome = n_s[1]
                            println("Creazione persona in corso..")
                            createPerson(nome, cognome)
                        }
                        println("checkPersonData: ${error.networkResponse.statusCode}")
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
            val stringRequest = JsonObjectRequest(Request.Method.POST, urlUser, bodyJSON,
                    { response ->
                        _currentPersonLiveData.value = getPersonFromResponse(response.toString())
                    },
                    { error -> println("createPerson: ${error.networkResponse.statusCode}") }
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
            val stringRequest = JsonObjectRequest(Request.Method.PUT, "$urlUser/$personTkn", bodyJSON,
                    { response ->
                        _currentPersonLiveData.value = getPersonFromResponse(response.toString())
                    },
                    { error -> println("updateCurrentPerson: ${error.networkResponse.statusCode}") }
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
            val stringRequest = JsonObjectRequest(Request.Method.PUT, "$urlUser/$personTkn", bodyJSON,
                    { response ->
                        _currentPersonLiveData.value = getPersonFromResponse(response.toString())
                    },
                    { error -> println("updateCurrentPersonPic: ${error.networkResponse.statusCode}") }
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
            val stringRequest = StringRequest(Request.Method.DELETE, "$urlUser/$personTkn",
                    { response ->
                        //setResponseInLiveData(response.toString())
                    },
                    { error -> println("deleteCurrentPersonData: ${error.networkResponse.statusCode}") }
            )
            queue.add(stringRequest)
        }

        performRequest(request)
        _currentPersonLiveData.value = Person() // empty person, allow UI to update
    }


    // ====================== TABLE ===========================

    fun getParticipantsOfTable(table: Table): ParticipantsListLiveData {
        val result = ParticipantsListLiveData()
        //result.max = table.participantsList.size

        val request = fun() {
            val stringRequest = StringRequest(Request.Method.GET,
                    "$urlTable/${table.id}/partecipants?gid=$personTkn",
                    { response ->
                        val participantsJSONs = JSONArray(response)
                        for (i in 0 until participantsJSONs.length()) {
                            result.add(getPersonFromResponse(participantsJSONs[i].toString()))
                        }
                        result.commit()
                    },
                    { error -> println("getParticipantsOfTable: ${error.networkResponse.statusCode}")})

            queue.add(stringRequest)
        }

        performRequest(request)
        return result
    }

    /**
     * Add current user to the given table
     */
    fun addParticipantToTable(table: Table, owner: Int = 0, onComplete: (Boolean) -> Unit): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        val request = fun() {
            val bodyJSON = JSONObject()
            bodyJSON.put("gid", personTkn)
            bodyJSON.put("owner", owner)

            val stringRequest = JsonObjectRequest( Request.Method.POST,
                    "$urlTable/${table.id}/partecipants", bodyJSON,
                    { response ->
                        result.value = true
                        onComplete(true)
                    },
                    { error ->
                        println("addParticipantToTable: ${error.networkResponse.statusCode}")
                        onComplete(false)
                    })

            queue.add(stringRequest)
        }

        performRequest(request)
        return result
    }

    fun deleteParticipantFromTable(table:Table,  onComplete: (Boolean) -> Unit): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        val request = fun() {
            val stringRequest = StringRequest( Request.Method.DELETE, "$urlTable/${table.id}/partecipants/$personTkn",
                    { response ->
                        result.value = true
                        onComplete(true)
                    },
                    { error ->
                        println("deleteParticipantFromTable: ${error.networkResponse.statusCode}")
                        onComplete(false)
                    })

            queue.add(stringRequest)
        }

        performRequest(request)
        return result
    }

    fun deleteTable(table:Table, onComplete: (Boolean) -> Unit): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        val request = fun() {
            val stringRequest = StringRequest( Request.Method.DELETE, "$urlTable/${table.id}/partecipants?gid=$personTkn",
                    { response ->
                        result.value = true
                        onComplete(true)
                    },
                    { error ->
                        println("deleteTable: ${error.networkResponse.statusCode}")
                        onComplete(false)
                    })

            queue.add(stringRequest)
        }

        performRequest(request)
        return result
    }
     /**
     *  task is basically a request needing the idToken for authentication on backend.
     */
    private fun performRequest( request: () -> Unit) {
        if ( personTkn.isEmpty() ) {
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
                profile_pic = result.optString("profile_pic"),
                isOwner = result.optBoolean("owner"),
        )
    }

    companion object {
        private var INSTANCE: UserRepository? = null

        fun getUserRepository(application: Application): UserRepository {
            return synchronized(UserRepository::class) {
                val newInstance = INSTANCE ?: UserRepository(application)
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}