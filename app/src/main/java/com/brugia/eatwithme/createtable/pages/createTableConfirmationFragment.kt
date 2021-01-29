package com.brugia.eatwithme.createtable.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.FormPage

class CreateTableConfirmationFragment: FormPage() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.create_table_confirmation, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView?.text = getString(R.string.success_title)
        subTitleTextView?.text = getString(R.string.success_subtitle)
        view.findViewById<Button>(R.id.confirmButton).setOnClickListener { onNextClicked() }
    }
}