package com.brugia.eatwithme.createtable.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.FormPage
import com.firebase.ui.auth.ui.InvisibleActivityBase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateTableDateFragment: FormPage() {

    private lateinit var dateInputView: TextInputEditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.create_table_date, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleTextView.text = getString(R.string.date_title)
        subTitleTextView.text = getString(R.string.date_subtitle)
    }

    private fun isNameValid():Boolean {
        return true
    }

    override fun isValid(): Boolean {
        return isNameValid()
    }
}