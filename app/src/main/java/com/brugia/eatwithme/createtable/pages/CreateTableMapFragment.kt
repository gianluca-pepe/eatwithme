package com.brugia.eatwithme.createtable.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.FormPage

class CreateTableMapFragment : FormPage() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_table_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
    }


    private fun isNameValid():Boolean {
        return true
    }

    override fun isValid(): Boolean {
        return isNameValid()
    }

}