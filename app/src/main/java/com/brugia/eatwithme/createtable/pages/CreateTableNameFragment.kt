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

class CreateTableNameFragment: FormPage() {

    private lateinit var nameInputView: TextInputEditText
    private lateinit var descriptionInputView: TextInputEditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.create_table_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previousButton?.visibility = View.INVISIBLE
        titleTextView.text = getString(R.string.name_title)
        subTitleTextView.text = getString(R.string.name_subtitle)

        nameInputView = view.findViewById(R.id.newTableNameInput)
        nameInputView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                isNameValid()
        }

        descriptionInputView = view.findViewById(R.id.newTableDescriptionInput)
    }

    private fun isNameValid():Boolean {
        val nameInputLayout = nameInputView.parent.parent as TextInputLayout
        if (nameInputView.text.toString().length < 3) {
            nameInputLayout.error = getString(R.string.table_name_length_error)
            return false
        } else
            nameInputLayout.error = ""

        return true
    }

    override fun isValid(): Boolean {
        return isNameValid()
    }
}