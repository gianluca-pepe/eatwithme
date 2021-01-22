
package com.brugia.eatwithme.data


import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brugia.eatwithme.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.Date
import org.w3c.dom.Document
import java.text.SimpleDateFormat
import java.util.*

/* Handles operations on tablesLiveData and holds details about it. */
class TablesDataSource(resources: Resources) {
    private val BATCHSIZE: Long = 10
    private val currentTableNumber: Long
        get() {
            tablesLiveData.value?.let {
                return it.size.toLong()
            }

            return 0
        }

    private val tablesLiveData: MutableLiveData<List<Table?>> by lazy {
        MutableLiveData<List<Table?>>()
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

    private lateinit var lastDocument: DocumentSnapshot
    private val initialAllTablesQuery = db.collection("Tables")
        .whereEqualTo("full", false)
        .whereGreaterThanOrEqualTo("timestamp", todayDate)
        .limit(BATCHSIZE)
    private var allTablesQuery = initialAllTablesQuery

    private val myNextTablesQuery = db.collection("Tables")
        .whereGreaterThanOrEqualTo("timestamp", todayDate)
        .whereArrayContains("participantsList", personID)
        .orderBy("timestamp")

    private val myPastTablesQuery = db.collection("Tables")
        .whereArrayContains("participantsList", personID)
        .whereLessThan("timestamp", todayDate)
        .orderBy("timestamp", Query.Direction.DESCENDING)

    lateinit var myNextTablesRegistration: ListenerRegistration
    lateinit var myPastTablesRegistration: ListenerRegistration


    // LiveData so views can observe and be notified when there's no more table to load
    private var _endReached: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val endReached: LiveData<Boolean> // constant so value can't be changed, real value is private
        get() = _endReached

    fun loadTablesBatch(refresh: Boolean = false) {
        _endReached.value = false
        if (this::lastDocument.isInitialized) {
            allTablesQuery = allTablesQuery.startAfter(lastDocument)
        }

        if (refresh) {
            allTablesQuery = initialAllTablesQuery
            tempList.clear()
        }

        allTablesQuery.get().addOnSuccessListener { results ->
            if (results.isEmpty) {
                _endReached.value = true
                println("fine raggiunta")
                return@addOnSuccessListener
            }

            println("query fatta con letture")
            updateTableList(tempList, results!!)
            tablesLiveData.postValue(tempList)
            lastDocument = results.last()
        }
    }

    fun listenMyTables() {
        myNextTablesRegistration = myNextTablesQuery.addSnapshotListener { results, e ->
            if (e != null) {
                println( "[My next tables list] Listen failed.")
                println(e)
                return@addSnapshotListener
            }
            myNextTablesList.clear()
            updateTableList(myNextTablesList, results!!)
            myNextTablesLiveData.postValue(myNextTablesList)
        }

        myPastTablesRegistration = myPastTablesQuery.addSnapshotListener { results, e ->
            if (e != null) {
                println("[My past tables list] Listen failed.")
                println(e)
                return@addSnapshotListener
            }
            myPastTablesList.clear()
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
            return tables.firstOrNull{ it?.id == id}
        }
        return null
    }

    fun getTableList(): LiveData<List<Table?>> {
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

