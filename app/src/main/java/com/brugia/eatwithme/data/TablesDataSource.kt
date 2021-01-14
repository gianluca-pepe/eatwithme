
package com.brugia.eatwithme.data


import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brugia.eatwithme.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/* Handles operations on tablesLiveData and holds details about it. */
class TablesDataSource(resources: Resources) {
    private val tablesLiveData: MutableLiveData<List<Table>> by lazy {
        MutableLiveData<List<Table>>()
    }
    private val myTablesLiveData: MutableLiveData<List<Table>> by lazy {
        MutableLiveData<List<Table>>()
    }
    private val db = Firebase.firestore
    private var tempList = mutableListOf<Table>()

    var myTablesList = mutableListOf<Table>()
    private val personID: String =  FirebaseAuth.getInstance().currentUser?.uid.toString()

    init {
        db.collection("Tables")
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

                        } catch(e:Exception) {
                            println(e)
                        }
                    }
                    tablesLiveData.postValue(tempList)
                    updateMyTables()
                }
    }

    /* Adds table to liveData and posts value. */
    fun addTable(table: Table) {
        val currentList = tablesLiveData.value
        if (currentList == null) {
            tablesLiveData.postValue(listOf(table))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, table)
            tablesLiveData.postValue(updatedList)
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
    * This function returns table in which the person appears
    * */
    fun updateMyTables(){
        db.collection("Tables")
                .whereArrayContains("participantsList", personID)
                //.orderBy("timestamp", Query.Direction.DESCENDING)
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

                            if (!newTable.isFull()) myTablesList.add(newTable)

                        } catch(e:Exception) {
                            println(e)
                        }
                    }
                    myTablesLiveData.postValue(myTablesList)
                }
    }

    fun getMyTablesList(): LiveData<List<Table>> {
        return myTablesLiveData
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

