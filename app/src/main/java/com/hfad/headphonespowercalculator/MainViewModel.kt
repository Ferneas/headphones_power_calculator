package com.hfad.headphonespowercalculator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel {
    val state = MutableStateFlow(
        MainViewModelState(
            "98",
            "32",
            "120",
            "0.00",
            "0.00",
            "0.00",
            "0.00"
        )
    )
    fun onSensitivityChange(sensitivity: String) {
        state.update {
            it.copy(sensitivity = if (sensitivity.toIntOrNull() in 0..999) sensitivity else "")
        }
    }
}