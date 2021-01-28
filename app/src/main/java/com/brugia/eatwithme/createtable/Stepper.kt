package com.brugia.eatwithme.createtable

import android.animation.ObjectAnimator
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import com.brugia.eatwithme.R

class Stepper(
        private val _view: View,
        private var _steps: List<Map<String, Int>>,
        private var _progressBar: ProgressBar ) {

    private var _currentStep = 0
    private val TEXT = "text"
    private val BTN = "button"
    private val progressStepIncrement = _progressBar.max / (_steps.size - 1)
    private val defaultColor = getColor(_view.context, R.color.quantum_black_secondary_text )
    private val accentColor = getColor(_view.context ,R.color.design_default_color_secondary)

    private var actualStepTextView: TextView? = null
        get() = getStepTextView(_currentStep)

    private var actualStepRadioButton: RadioButton? = null
        get() = getStepRadioButton(_currentStep)

    private fun getStepTextView(position: Int): TextView? {

        return _steps[position][TEXT]?.let { _view.findViewById(it) }
    }

    private fun getStepRadioButton(position: Int): RadioButton? {
        if (position >= _steps.size) return null
        return _steps[position][BTN]?.let { _view.findViewById(it) }
    }

    fun completeStep() {
        if (_currentStep + 1 >= _steps.size) return

        actualStepTextView?.setTextColor(accentColor)
        actualStepRadioButton?.buttonDrawable = getDrawable(_view.context,R.drawable.ic_baseline_check_circle_24)
        actualStepRadioButton?.isChecked = true

        ObjectAnimator.ofInt(_progressBar, "progress",
                _progressBar.progress + progressStepIncrement)
                .setDuration(300)
                .start()
        _currentStep += 1
    }

    fun stepBack() {
        _currentStep -= 1
        actualStepTextView?.setTextColor(defaultColor)
        actualStepRadioButton?.buttonDrawable = getDrawable(_view.context, R.drawable.ic_baseline_radio_button_unchecked_24)
        actualStepRadioButton?.isChecked = false
        _progressBar.incrementProgressBy(-progressStepIncrement)
    }
}