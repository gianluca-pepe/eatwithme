
package com.brugia.eatwithme.tablelist

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.TablesDataSource
import com.brugia.eatwithme.data.mealcategory.MealCategory

class TablesListViewModel(private val dataSource: TablesDataSource) : ViewModel() {
    val tablesLiveData = dataSource.getTableList()
    val nearbyTables = dataSource.getNearbyTableList()
    val endReached = dataSource.endReached
    val BATCHSIZE = dataSource.BATCHSIZE
    val location = MutableLiveData<Location?>(null)
    val radius = MutableLiveData<Int?>(null)
    val address = MutableLiveData("")
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

    fun loadMoreTables(location: Location? = this.location.value, radius: Int? = this.radius.value,
            mealCategory: Int = MealCategory.ALL) {
        if (location != null)
            dataSource.loadTablesBatchWithLocation(location, radius?: 0, categoryID = mealCategory)
        else
            dataSource.loadTablesBatch(categoryID = mealCategory)
    }

    fun refresh(location: Location?= this.location.value, radius: Int?= this.radius.value,
        mealCategory: Int = MealCategory.ALL) {
        if (location != null)
            dataSource.loadTablesBatchWithLocation(location, radius?: 0, true, mealCategory)
        else
            dataSource.loadTablesBatch(true, categoryID = mealCategory)
    }

    fun loadNearby(location: Location) {
        dataSource.loadNearbyTables(location)
    }
}

class TablesListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

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
