package com.hfad.headphonespowercalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class MainViewModelState(
    val sensitivity: String,
    val impedance: String,
    val loudness: String,
    val resultPower: String,
    val resultVoltage: String,
    val resultCurrent: String,
    val resultEquiv: String
)