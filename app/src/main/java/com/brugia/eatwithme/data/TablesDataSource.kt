
package com.brugia.eatwithme.data


import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/* Handles operations on tablesLiveData and holds details about it. */
class TablesDataSource(resources: Resources) {
    private val initialTableList = tableList(resources)
    private val tablesLiveData = MutableLiveData(initialTableList)

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
    fun getTableForId(id: Long): Table? {
        tablesLiveData.value?.let { tables ->
            return tables.firstOrNull{ it.id == id}
        }
        return null
    }

    fun getTableList(): LiveData<List<Table>> {
        return tablesLiveData
    }

    /* Returns a random table asset for tables that are added. */
    fun getRandomTableImageAsset(): Int? {
        val randomNumber = (initialTableList.indices).random()
        return initialTableList[randomNumber].image
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

