
package com.brugia.eatwithme.tablelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.MainFragment
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.data.TablesDataSource
import com.brugia.eatwithme.mytables.NextTables
import com.brugia.eatwithme.mytables.PastTables

class TablesListViewModel(val dataSource: TablesDataSource) : ViewModel() {

    val tablesLiveData = dataSource.getTableList()
    val myNextTablesLiveData = dataSource.getMyNextTablesList()
    val myPastTablesLiveData = dataSource.getMyPastTablesList()
/*
    /* If the name and description are present, create new Table and add it to the datasource */
    fun insertTable(
            tableName: String?,
            tableDescription: String?,
            tableImage: Int?,
            tableDate: String,
            tableHour: String,
            maxPartecipants: Int,
            numPartecipants: Int,
            city: String,
    ) {
        if (tableName == null || tableDescription == null) {
            return
        }

        //val image = dataSource.getRandomTableImageAsset()
        val newTable = Table(
                Random.nextLong(),
                tableName,
                tableDescription,
                tableImage,
                tableDate,
                tableHour,
                maxPartecipants,
                numPartecipants,
                city
        )

        dataSource.addTable(newTable)
    }
 */

    fun populate() {
        dataSource.listenRemote()
    }
    /**
     * Tells the datasource to stop listening for updates
     * Useful because we get charged for listening updates (should be free for low usage)
     */
    fun removeListeners() {
        dataSource.allTablesRegistration.remove()
        dataSource.myPastTablesRegistration.remove()
        dataSource.myNextTablesRegistration.remove()
    }
}

class TablesListViewModelFactory(private val context: MainFragment) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TablesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TablesListViewModel(
                    dataSource = TablesDataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MyNextTablesListViewModelFactory(private val context: NextTables) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TablesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TablesListViewModel(
                    dataSource = TablesDataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MyPastTablesListViewModelFactory(private val context: PastTables) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TablesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TablesListViewModel(
                    dataSource = TablesDataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}