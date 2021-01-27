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
    lateinit var titleTextView: TextView
    lateinit var subTitleTextView: TextView
    var nextButton: Button? = null
    var previousButton: Button? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleTextView = view.findViewById(R.id.createTablePageTitle)
        subTitleTextView = view.findViewById(R.id.createTablePageSubtitle)
        nextButton = view.findViewById(R.id.button_next)
        nextButton?.setOnClickListener { onNextClicked() }
        previousButton = view.findViewById(R.id.button_previous)
        previousButton?.setOnClickListener { onPreviousClicked() }
    }

    open fun isValid(): Boolean = true
}