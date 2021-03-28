package com.brugia.eatwithme.mytables

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.TablesDataSource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyTablesListViewModel (private val dataSource: TablesDataSource): ViewModel() {
    val myNextTablesLiveData = dataSource.getMyNextTablesList()
    val myPastTablesLiveData = dataSource.getMyPastTablesList()
    init {
        dataSource.listenMyTables()
    }

    /**
     * Tells the datasource to stop listening for updates
     * Useful because we get charged for listening updates (should be free for low usage)
     */
    fun removeListeners() {
        //dataSource.myPastTablesRegistration.remove()
        //dataSource.myNextTablesRegistration.remove()
    }

    fun listenMyTables() {
       //dataSource.listenMyTables()
    }

    fun exitAllTables(): MutableLiveData<Int> {
        val db = Firebase.firestore
        val auth_id = Firebase.auth.uid
        val num_done = MutableLiveData(myNextTablesLiveData.value?.size ?: 0)

        myNextTablesLiveData.value?.forEach {
            db.collection("Tables").document(it.id).update(
                    "participantsList",
                    FieldValue.arrayRemove(auth_id)
            ).addOnFailureListener {
                println("uscita tavolo fallita" + it)
            }.addOnSuccessListener {
                println("uscita tavolo riuscita")
                num_done.value = num_done.value?.minus(1)
            }
        }

        return num_done
    }
}

class MyTablesListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyTablesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyTablesListViewModel(
                    dataSource = TablesDataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}