
package com.brugia.eatwithme.tablelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.MainFragment
import com.brugia.eatwithme.data.TablesDataSource
import com.brugia.eatwithme.mytables.MyTablesFragment

class TablesListViewModel(val dataSource: TablesDataSource) : ViewModel() {

    val tablesLiveData = dataSource.getTableList()
    val myTablesLiveData = dataSource.getMyTablesList()
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

class MyTablesListViewModelFactory(private val context: MyTablesFragment) : ViewModelProvider.Factory {

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