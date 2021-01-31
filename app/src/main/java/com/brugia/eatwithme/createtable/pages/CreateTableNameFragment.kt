package com.brugia.eatwithme.createtable.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.CreateTableViewModel
import com.brugia.eatwithme.createtable.FormPage
import com.firebase.ui.auth.ui.InvisibleActivityBase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class CreateTableNameFragment: FormPage() {

    private val newTableViewModel by activityViewModels<CreateTableViewModel>()
    private lateinit var nameInputView: TextInputEditText
    private lateinit var descriptionInputView: TextInputEditText
    private lateinit var maxParticipantsSeekBar: SeekBar
    private lateinit var maxParticipantsTextView: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.create_table_name, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleTextView?.text = getString(R.string.name_title)
        subTitleTextView?.text = getString(R.string.name_subtitle)

        nameInputView = view.findViewById(R.id.newTableNameInput)
        nameInputView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) isNameValid()
        }

        descriptionInputView = view.findViewById(R.id.newTableDescriptionInput)

        newTableViewModel.table.observe(viewLifecycleOwner, {
            nameInputView.setText(it.name)
            descriptionInputView.setText(it.description)
            maxParticipantsSeekBar.progress = it.maxParticipants?.minus(2) ?: 0
            maxParticipantsTextView.text = it.maxParticipants.toString()
        })

        maxParticipantsSeekBar = view.findViewById(R.id.maxParticipantsBar)
        maxParticipantsTextView = view.findViewById(R.id.maxParticipantsValue)

        maxParticipantsSeekBar.progress = 2
        maxParticipantsSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxParticipantsTextView.text = (progress + 2).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun isNameValid():Boolean {
        val nameInputLayout = nameInputView.parent.parent as TextInputLayout
        if (nameInputView.text.toString().length < 3) {
            nameInputLayout.error = getString(R.string.table_name_length_error)
            return false
        } else
            nameInputLayout.error = null

        return true
    }

    override fun isValid(): Boolean {
        if (isNameValid()) {
            newTableViewModel.table.removeObservers(viewLifecycleOwner)
            newTableViewModel.name = nameInputView.text.toString()
            newTableViewModel.description = descriptionInputView.text.toString()
            newTableViewModel.maxParticipants = maxParticipantsSeekBar.progress + 2
            return true
        }

        return false
    }
}