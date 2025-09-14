package com.hfad.headphonespowercalculator.data

data class HpcUiState(
    val sensitivity: String,
    val impedance: String,
    val loudness: String,
    val resultPower: String,
    val resultVoltage: String,
    val resultCurrent: String,
    val resultEquiv: String
)