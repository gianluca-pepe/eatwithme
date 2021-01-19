
package com.brugia.eatwithme.data


import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brugia.eatwithme.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
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

    private val allTablesQuery = db.collection("Tables")
            .whereGreaterThanOrEqualTo("timestamp", todayDate)

    private val myNextTablesQuery = allTablesQuery
            .whereArrayContains("participantsList", personID)
            .orderBy("timestamp")

    private val myPastTablesQuery = db.collection("Tables")
            .whereArrayContains("participantsList", personID)
            .whereLessThan("timestamp", todayDate)
            .orderBy("timestamp", Query.Direction.DESCENDING)

    lateinit var allTablesRegistration: ListenerRegistration
    lateinit var myNextTablesRegistration: ListenerRegistration
    lateinit var myPastTablesRegistration: ListenerRegistration

    fun listenRemote() {
        allTablesRegistration = allTablesQuery.addSnapshotListener { results, e ->
            if (e != null) {
                println( "[All tables list] Listen failed.")
                println(e)
                return@addSnapshotListener
            }

            updateTableList(tempList, results!!)
            tablesLiveData.postValue(tempList)
        }

        myNextTablesRegistration = myNextTablesQuery.addSnapshotListener { results, e ->
            if (e != null) {
                println( "[My next tables list] Listen failed.")
                println(e)
                return@addSnapshotListener
            }

            updateTableList(myNextTablesList, results!!)
            myNextTablesLiveData.postValue(myNextTablesList)
        }

        myPastTablesRegistration = myPastTablesQuery.addSnapshotListener { results, e ->
            if (e != null) {
                println("[My past tables list] Listen failed.")
                println(e)
                return@addSnapshotListener
            }

            updateTableList(myPastTablesList, results!!)
            myPastTablesLiveData.postValue(myPastTablesList)
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
    fun getMyNextTablesList(): LiveData<List<Table>> {
        return myNextTablesLiveData
    }

    /*
    * This function returns next table (w.r.t. today included) in which the person appears
    * */
    fun getMyPastTablesList(): LiveData<List<Table>> {
        return myPastTablesLiveData
    }

    /* Returns a random table asset for tables that are added.
    fun getRandomTableImageAsset(): Int? {
        val randomNumber = (initialTableList.indices).random()
        return initialTableList[randomNumber].image
    }
     */

    fun getParticipantsList(table: Table): ParticipantsListLiveData {
        val result = ParticipantsListLiveData()
        result.max = table.participantsList.size
        for (id in table.participantsList) {
            val personRef = db.collection("Users").document(id)
            result.add(personRef)
        }

        return result
    }

    fun getParticipantsList(tableId: String): ParticipantsListLiveData {
        val tableRef = db.collection("Tables").document(tableId)
        val result = ParticipantsListLiveData()
        tableRef.addSnapshotListener { tableSnapshot, error ->
            if (tableSnapshot != null && tableSnapshot.exists()) {
                val idList = tableSnapshot.get("participantsList") as ArrayList<String>
                result.max = idList.size
                for (id in idList) {
                    val personRef = db.collection("Users").document(id)
                    result.add(personRef)
                }
            } else {
                println(error)
            }
        }

        return result
    }

    private fun updateTableList(list: MutableList<Table>, docs: QuerySnapshot) {
        list.clear()
        for(doc in docs) {
            try {
                val newTable = Table(doc)
                list.add(newTable)
                //if (!newTable.isFull()) list.add(newTable)
            } catch (e: Exception) {
                println(e)
            }
        }
    }

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

