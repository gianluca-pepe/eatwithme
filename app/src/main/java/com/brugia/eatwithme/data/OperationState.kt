package com.brugia.eatwithme.data

import androidx.lifecycle.MutableLiveData

/**
 * The state of an operation involving both firebase and python backend.
 * Observers will be notified with a True value only when both services will be in a
 * success state.
 */
class OperationState(): MutableLiveData<Boolean>() {
    var firebase: Boolean? = null
        set(value) {
            field = value
            updateState()
        }
    var py: Boolean? = null
        set(value) {
            field = value
            updateState()
        }

    private fun updateState() {
        // update state only when both services returned their state
        if (firebase != null && py != null) {
            value = firebase!! && py!!
        }
    }
}