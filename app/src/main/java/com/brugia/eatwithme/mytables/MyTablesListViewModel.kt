package com.brugia.eatwithme.mytables

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.TablesDataSource
import com.brugia.eatwithme.tablelist.TablesListViewModel

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