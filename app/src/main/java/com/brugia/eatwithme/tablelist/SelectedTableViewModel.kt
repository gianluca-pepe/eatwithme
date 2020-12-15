package com.brugia.eatwithme.tablelist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brugia.eatwithme.data.Table
import com.brugia.eatwithme.location.LocationViewModel

class SelectedTableViewModel: ViewModel() {
    private val _selectedTable = MutableLiveData<Table>()

    fun setSelectedTable(table: Table) {
        _selectedTable.value = table
    }

    fun getSelectedTable() = _selectedTable
}
