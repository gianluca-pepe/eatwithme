package com.brugia.eatwithme.createtable

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.brugia.eatwithme.R
import kotlin.reflect.KFunction

open class FormPage: Fragment() {

    lateinit var onNextClicked: () -> Unit
    lateinit var  onPreviousClicked: () -> Unit
    var titleTextView: TextView? = null
    var subTitleTextView: TextView? = null
    var nextButton: Button? = null
    var previousButton: Button? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView = view.findViewById(R.id.createTablePageTitle)
        subTitleTextView = view.findViewById(R.id.createTablePageSubtitle)
        nextButton = view.findViewById(R.id.button_next)
        nextButton?.setOnClickListener { onSubmit() }
        previousButton = view.findViewById(R.id.button_previous)
        previousButton?.setOnClickListener { onBack() }
    }

    open fun isValid(): Boolean = true

    open fun onSubmit() {
        onNextClicked()
    }

    open fun onBack() {
        onPreviousClicked()
    }
}