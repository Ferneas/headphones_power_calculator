package com.hfad.headphonespowercalculator

import androidx.lifecycle.ViewModel
import com.hfad.headphonespowercalculator.data.HpcUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.ranges.contains
import kotlin.text.toDoubleOrNull
import kotlin.text.toIntOrNull

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        HpcUiState(
            "98",
            "32",
            "120",
            "0.00",
            "0.00",
            "0.00",
            "0.00"
        )
    )
    val state: StateFlow<HpcUiState> = _state.asStateFlow()

    init {
        updateAllResults()
    }
    fun onSensitivityChange(sensitivity: String) {
        _state.update {
            it.copy(sensitivity = if (sensitivity.toIntOrNull() in 0..999) sensitivity else "")
        }
        updateAllResults()
    }

    fun onImpedanceChange(impedance: String) {
        _state.update {
            it.copy(impedance = if (impedance.toIntOrNull() in 0..999) impedance else "")
        }
        updateAllResults()
    }

    fun onLoudnessChange(loudness: String) {
        _state.update {
            it.copy(loudness = if (loudness.toIntOrNull() in 0..150) loudness else "")
        }
        updateAllResults()
    }

    fun calculateMilliwatts(loudness: String, sensitivity: String): String {
        val loudness = loudness.toDoubleOrNull() ?: 0.00
        val sensitivity = sensitivity.toDoubleOrNull() ?: 0.00
        return (10.0.pow((loudness - sensitivity) / 10)).toString()
    }

    fun calculateVoltage(loudness: String, sensitivity: String, impedance: String): String {
        val resultMilliwatt =
            (calculateMilliwatts(loudness, sensitivity).toDoubleOrNull() ?: 0.00) * 0.001
        val impedance = impedance.toDoubleOrNull() ?: 0.00
        return (sqrt(resultMilliwatt * impedance)).toString()
    }

    fun calculateCurrent(loudness: String, sensitivity: String, impedance: String): String {
        val resultVoltage = (calculateVoltage(loudness, sensitivity, impedance)).toDoubleOrNull() ?: 0.00
        val impedance = impedance.toDoubleOrNull() ?: 0.00
        return ((resultVoltage / impedance) * 1000).toString()
    }

    fun calculateEquiv(sensitivity: String, impedance: String): String {
        val sensitivity = sensitivity.toDoubleOrNull() ?: 0.00
        val impedance = impedance.toDoubleOrNull() ?: 0.00
        return (sensitivity + (10 * (log10(1000 / impedance)))).toString()
    }

    fun updateAllResults() {
        _state.update { currentState ->
            currentState.copy(
                resultPower = calculateMilliwatts(currentState.loudness, currentState.sensitivity),
                resultVoltage =
                        calculateVoltage(
                            currentState.loudness,
                            currentState.sensitivity,
                            currentState.impedance
                        ),
                resultCurrent =
                        calculateCurrent(
                            currentState.loudness,
                            currentState.sensitivity,
                            currentState.impedance
                        ),
                resultEquiv = calculateEquiv(currentState.sensitivity, currentState.impedance)
            )
        }
    }
}