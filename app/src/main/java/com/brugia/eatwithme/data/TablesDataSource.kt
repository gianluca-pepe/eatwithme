
package com.brugia.eatwithme.data


import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brugia.eatwithme.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.Date
import java.text.SimpleDateFormat
import java.util.*

/* Handles operations on tablesLiveData and holds details about it. */
class TablesDataSource(resources: Resources) {
    private val tablesLiveData: MutableLiveData<List<Table>> by lazy {
        MutableLiveData<List<Table>>()
    }
    private val myNextTablesLiveData: MutableLiveData<List<Table>> by lazy {
        MutableLiveData<List<Table>>()
    }
    private val myPastTablesLiveData: MutableLiveData<List<Table>> by lazy {
        MutableLiveData<List<Table>>()
    }
    private val db = Firebase.firestore
    private var tempList = mutableListOf<Table>()

    private val todayDate = Timestamp.now()


    var myNextTablesList = mutableListOf<Table>()
    var myPastTablesList = mutableListOf<Table>()
    private val personID: String =  FirebaseAuth.getInstance().currentUser?.uid.toString()

    init {

        db.collection("Tables")
                .whereGreaterThanOrEqualTo("timestamp", todayDate)
                .get()
                .addOnSuccessListener { results ->

                    for(doc in results) {
                        try {
                            val newTable = Table(
                                    id = doc.id,
                                    ownerId = doc.getString("ownerId"),
                                    name = doc.getString("name"),
                                    description = doc.getString("description"),
                                    timestamp = doc.getTimestamp("timestamp"),
                                    location = hashMapOf(
                                            "latlog" to doc.getGeoPoint("location.latlog"),
                                            "label" to doc.getString("location.label")
                                    ),
                                    maxParticipants = doc.getLong("maxParticipants")?.toInt(),
                                    participantsList = doc.get("participantsList") as List<String>,
                                    image = R.drawable.logo_login
                            )

                            if (!newTable.isFull()) tempList.add(newTable)

                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                    tablesLiveData.postValue(tempList)
                    getMyNextTables()
                    getMyPastTables()
                }
    }

    /* Adds table to liveData and posts value. */
    fun addTable(table: Table) {
        val currentList = tablesLiveData.value
        val myNextList = myNextTablesLiveData.value
        if (currentList == null) {
            tablesLiveData.postValue(listOf(table))
            myNextTablesLiveData.postValue(listOf(table))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, table)
            tablesLiveData.postValue(updatedList)

            val myUpdatedList = myNextList!!.toMutableList()
            myUpdatedList.add(0, table)
            myNextTablesLiveData.postValue(myUpdatedList)
        }
    }

    /* Removes table from liveData and posts value. */
    fun removeTable(table: Table) {
        val currentList = tablesLiveData.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.remove(table)
            tablesLiveData.postValue(updatedList)
        }
    }

    /* Returns table given an ID. */
    fun getTableForId(id: String): Table? {
        tablesLiveData.value?.let { tables ->
            return tables.firstOrNull{ it.id == id}
        }
        return null
    }

    fun getTableList(): LiveData<List<Table>> {
        return tablesLiveData
    }

    /*
    * This function returns next table (w.r.t. today included) in which the person appears
    * */
    private fun getMyNextTables(){

        db.collection("Tables")
                .whereGreaterThanOrEqualTo("timestamp", todayDate)
                .whereArrayContains("participantsList", personID)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { results ->

                    for(doc in results) {
                        try {
                            val newTable = Table(
                                    id = doc.id,
                                    ownerId = doc.getString("ownerId"),
                                    name = doc.getString("name"),
                                    description = doc.getString("description"),
                                    timestamp = doc.getTimestamp("timestamp"),
                                    location = hashMapOf(
                                            "latlog" to doc.getGeoPoint("location.latlog"),
                                            "label" to doc.getString("location.label")
                                    ),
                                    maxParticipants = doc.getLong("maxParticipants")?.toInt(),
                                    participantsList = doc.get("participantsList") as List<String>,
                                    image = R.drawable.logo_login
                            )

                            if (!newTable.isFull()) myNextTablesList.add(newTable)

                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                    myNextTablesLiveData.postValue(myNextTablesList)
                }
    }

    fun getMyNextTablesList(): LiveData<List<Table>> {
        return myNextTablesLiveData
    }

    /*
    * This function returns next table (w.r.t. today included) in which the person appears
    * */
    private fun getMyPastTables(){
        db.collection("Tables")
                .whereArrayContains("participantsList", personID)
                .whereLessThan("timestamp", todayDate)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { results ->

                    for(doc in results) {
                        try {
                            val newTable = Table(
                                    id = doc.id,
                                    ownerId = doc.getString("ownerId"),
                                    name = doc.getString("name"),
                                    description = doc.getString("description"),
                                    timestamp = doc.getTimestamp("timestamp"),
                                    location = hashMapOf(
                                            "latlog" to doc.getGeoPoint("location.latlog"),
                                            "label" to doc.getString("location.label")
                                    ),
                                    maxParticipants = doc.getLong("maxParticipants")?.toInt(),
                                    participantsList = doc.get("participantsList") as List<String>,
                                    image = R.drawable.logo_login
                            )

                            if (!newTable.isFull()) myPastTablesList.add(newTable)

                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                    myPastTablesLiveData.postValue(myPastTablesList)
                }
    }
    fun getMyPastTablesList(): LiveData<List<Table>> {
        return myPastTablesLiveData
    }
    /* Returns a random table asset for tables that are added.
    fun getRandomTableImageAsset(): Int? {
        val randomNumber = (initialTableList.indices).random()
        return initialTableList[randomNumber].image
    }
     */

    companion object {
        private var INSTANCE: TablesDataSource? = null

        fun getDataSource(resources: Resources): TablesDataSource {
            return synchronized(TablesDataSource::class) {
                val newInstance = INSTANCE ?: TablesDataSource(resources)
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}

