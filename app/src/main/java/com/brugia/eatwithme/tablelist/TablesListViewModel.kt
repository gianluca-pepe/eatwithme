
package com.brugia.eatwithme.tablelist

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.MainFragment
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.data.TablesDataSource
import com.brugia.eatwithme.mytables.NextTables
import com.brugia.eatwithme.mytables.PastTables

class TablesListViewModel(private val dataSource: TablesDataSource) : ViewModel() {
    val tablesLiveData = dataSource.getTableList()
    val endReached = dataSource.endReached
    val BATCHSIZE = dataSource.BATCHSIZE
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

    fun loadMoreTables(location: Location? = null, radius: Int? = null) {
        if (location != null && radius != null)
            dataSource.loadTablesBatchWithLocation(location, radius)
        else
            dataSource.loadTablesBatch()
    }

    fun refresh(location: Location?= null, radius: Int?= null) {
        if (location != null && radius != null)
            dataSource.loadTablesBatchWithLocation(location, radius, true)
        else
            dataSource.loadTablesBatch(true)
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
