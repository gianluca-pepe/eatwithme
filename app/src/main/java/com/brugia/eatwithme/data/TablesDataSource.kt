
package com.brugia.eatwithme.data


import android.content.res.Resources
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brugia.eatwithme.data.mealcategory.MealCategory
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

/* Handles operations on tablesLiveData and holds details about it. */
class TablesDataSource(resources: Resources) {
    val BATCHSIZE: Long = 10
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

    private val nearbyTablesLiveData: MutableLiveData<List<Table?>> by lazy {
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
    private val initialTablesQueryLocation = db.collection("Tables")
            // not working
        //.whereGreaterThanOrEqualTo("timestamp", todayDate)
        //.orderBy("timestamp")
        .whereEqualTo("full", false)
        .orderBy("geoHash")
        .limit(BATCHSIZE)

    private val initialTablesQuery = db.collection("Tables")
        .whereEqualTo("full", false)
        .whereGreaterThanOrEqualTo("timestamp", todayDate)
        .orderBy("timestamp")

    private var allTablesQueryLocation = initialTablesQueryLocation
    private var allTablesQuery = initialTablesQuery

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

    private fun refreshAllTablesList() {
        allTablesQueryLocation = initialTablesQueryLocation
        allTablesQuery = initialTablesQuery
        tempList.clear()
    }

    fun loadTablesBatchWithLocation(location: Location, radius: Int, refresh: Boolean = false,
                                    categoryID: Int = MealCategory.ALL) {
        _endReached.value = false

        if (refresh) refreshAllTablesList()

        val center = GeoLocation(location.latitude, location.longitude)
        val radiusInMeters = (radius * 1000).toDouble()
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
        // a separate query for each pair. There can be up to 9 pairs of bounds
        // depending on overlap, but in most cases there are 4.
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMeters)
        for (b in bounds) {
            var q = allTablesQueryLocation.startAt(b.startHash).endAt(b.endHash)

            if (this::lastDocument.isInitialized && !refresh) {
                q = q.startAfter(lastDocument)
            }

            tasks.add(q.get())
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                var count = 0
                for (task in tasks) {
                    for (doc in task.result) {

                        val tableLat = doc.getDouble("restaurant.geometry.location.lat")!!
                        val tableLong = doc.getDouble("restaurant.geometry.location.lng")!!

                        // We have to filter out a few false positives due to GeoHash
                        // accuracy, but most will match
                        val docLocation = GeoLocation(tableLat, tableLong)
                        val distanceInM =
                            GeoFireUtils.getDistanceBetween(docLocation, center)
                        if (distanceInM <= radiusInMeters) {
                            val newTable = Table(doc)
                            if (    newTable.timestamp!! >= todayDate &&
                                    (categoryID == MealCategory.ALL || newTable.getCategory() == categoryID)
                            ) {
                                newTable.distance = distanceInM
                                tempList.add(newTable)
                            }
                            lastDocument = doc
                            count++
                        }
                    }
                }
                if (count == 0) {
                    _endReached.value = true
                    println("fine raggiunta")

                    if (refresh) tablesLiveData.postValue(tempList)
                    return@addOnCompleteListener
                }
                tablesLiveData.postValue(tempList)
            }

    }

    fun loadTablesBatch(refresh: Boolean = false, categoryID: Int = MealCategory.ALL ) {
        _endReached.value = false
        if (this::lastDocument.isInitialized && !refresh) {
            allTablesQuery = allTablesQuery.startAfter(lastDocument)
        }

        if (refresh) refreshAllTablesList()

        allTablesQuery.get().addOnSuccessListener { results ->
            if (results.isEmpty) {
                _endReached.value = true
                println("fine raggiunta")
                if (refresh) tablesLiveData.postValue(tempList)
                return@addOnSuccessListener
            }

            println("query fatta con letture")
            updateTableList(tempList, results!!, categoryID)
            tablesLiveData.postValue(tempList)
            lastDocument = results.last()
        }. addOnFailureListener {
            println(it)
        }
    }

    private val nearbyQuery = db.collection("Tables")
            .whereEqualTo("full", false)
            .orderBy("geoHash")
            .limit(5)

    fun loadNearbyTables(location: Location) {
        val nearbyTableTempList = mutableListOf<Table>()
        val center = GeoLocation(location.latitude, location.longitude)
        val radiusInMeters = (50 * 1000).toDouble()
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
        // a separate query for each pair. There can be up to 9 pairs of bounds
        // depending on overlap, but in most cases there are 4.
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMeters)
        for (b in bounds) {
            var q = nearbyQuery.startAt(b.startHash).endAt(b.endHash)
            tasks.add(q.get())
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener {
                    for (task in tasks) {
                        for (doc in task.result) {

                            val tableLat = doc.getDouble("restaurant.geometry.location.lat")!!
                            val tableLong = doc.getDouble("restaurant.geometry.location.lng")!!

                            // We have to filter out a few false positives due to GeoHash
                            // accuracy, but most will match
                            val docLocation = GeoLocation(tableLat, tableLong)
                            val distanceInM =
                                    GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInMeters) {
                                val newTable = Table(doc)
                                if ( newTable.timestamp!! >= todayDate ) {
                                    newTable.distance = distanceInM
                                    nearbyTableTempList.add(newTable)
                                }
                            }
                        }
                    }
                    nearbyTablesLiveData.postValue(nearbyTableTempList)
                }
    }

    fun listenMyTables() {
        myNextTablesRegistration = myNextTablesQuery.addSnapshotListener { results, e ->
            if (e != null) {
                println("[My next tables list] Listen failed.")
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

    fun getNearbyTableList(): LiveData<List<Table?>> {
        return nearbyTablesLiveData
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

    private fun updateTableList(list: MutableList<Table>, docs: QuerySnapshot,
                                categoryID: Int = MealCategory.ALL) {
        for(doc in docs) {
            try {
                val newTable = Table(doc)
                if (categoryID == MealCategory.ALL || newTable.getCategory() == categoryID)
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

