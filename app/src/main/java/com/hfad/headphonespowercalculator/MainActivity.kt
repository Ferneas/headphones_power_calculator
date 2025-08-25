package com.hfad.headphonespowercalculator

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.woof.ui.theme.HeadphonesPowerCalculatorTheme
import com.hfad.headphonespowercalculator.data.LoudnessInfo
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.text.toDoubleOrNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeadphonesPowerCalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HapApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HapApp(modifier: Modifier = Modifier) {
    var sensitivity by remember { mutableStateOf("98") }
    var impedance by remember { mutableStateOf("32") }
    var loudness by remember { mutableStateOf("120") }

    var resultPower = calculateMilliwatts(loudness, sensitivity)
    var resultVoltage = calculateVoltage(resultPower, impedance)
    var resultCurrent = calculateCurrent(resultVoltage, impedance)
    var resultEquiv = calculateEquiv(sensitivity, impedance)

    Column(
        modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(top = dimensionResource(R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(Modifier.weight(1f))

        Card(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                .padding(
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small),
                )
                .background(color = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(16.dp)
        ) {
            EditNumberField(
                label = R.string.sensitivity,
                value = sensitivity,
                onValueChange = {
                    sensitivity = if (it.toIntOrNull() in 0..999) it else ""
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(
                        bottom = dimensionResource(R.dimen.padding_medium),
                        top = dimensionResource(R.dimen.padding_medium)
                    )
                    .fillMaxWidth()
            )
            EditNumberField(
                label = R.string.impedance,
                value = impedance,
                onValueChange = {
                    impedance = if (it.toIntOrNull() in 0..999) it else ""
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(bottom = dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth()
            )
            EditNumberField(
                label = R.string.loudness,
                value = loudness,
                onValueChange = {
                    loudness = if (it.toIntOrNull() in 0..150) it else ""
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(bottom = dimensionResource(R.dimen.padding_medium))
                    .fillMaxWidth()
            )
            VolumeInfo(loudness)
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

        Card(
            modifier = Modifier
                .padding(
                    start = dimensionResource(R.dimen.padding_small),
                    end = dimensionResource(R.dimen.padding_small),

                    )
                .background(color = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(16.dp)
        ) {
            StatsGrid(
                resultPower = resultPower,
                resultVoltage = resultVoltage,
                resultCurrent = resultCurrent,
                resultEquiv = resultEquiv
            )
        }
        Spacer(Modifier.weight(2f))
    }
}

fun calculateEquiv(sensitivity: String, impedance: String): Double {
    val sensitivity = sensitivity.toDoubleOrNull() ?: 0.00
    val impedance = impedance.toDoubleOrNull() ?: 0.00
    return sensitivity + (10 * (log10(1000 / impedance)))
}

fun calculateCurrent(resultVoltage: Double, impedance: String): Double {
    val impedance = impedance.toDoubleOrNull() ?: 0.00
    return (resultVoltage / impedance) * 1000
}

fun calculateVoltage(requiredPower: Double, impedance: String): Double {
    val requiredPower = requiredPower * 0.001
    val impedance = impedance.toDoubleOrNull() ?: 0.00
    return sqrt(requiredPower * impedance)
}

fun calculateMilliwatts(loudness: String, sensitivity: String): Double {
    val loudness = loudness.toDoubleOrNull() ?: 0.00
    val sensitivity = sensitivity.toDoubleOrNull() ?: 0.00
    return 10.0.pow((loudness - sensitivity) / 10)
}

@Composable
fun VolumeInfo(loudness: String) {

    if (loudness.toIntOrNull() in 86..150) {
        var timeRecommendation = when (loudness.toInt()) {
            in 86..88 -> LoudnessInfo(R.string.high_loudness_1_head, R.string.high_loudness_1_body)
            in 89..91 -> LoudnessInfo(R.string.high_loudness_2_head, R.string.high_loudness_2_body)
            in 92..94 -> LoudnessInfo(R.string.high_loudness_3_head, R.string.high_loudness_3_body)
            in 95..97 -> LoudnessInfo(R.string.high_loudness_4_head, R.string.high_loudness_4_body)
            in 98..103 -> LoudnessInfo(
                R.string.very_high_loudness_1_head,
                R.string.very_high_loudness_1_body
            )

            in 104..109 -> LoudnessInfo(
                R.string.very_high_loudness_2_head,
                R.string.very_high_loudness_2_body
            )

            in 110..120 -> LoudnessInfo(
                R.string.very_high_loudness_3_head,
                R.string.very_high_loudness_3_body
            )

            else -> LoudnessInfo(R.string.extrime_loudness_head, R.string.extrime_loudness_body)
        }
        Column(
            modifier = Modifier
                .padding(
                    top = dimensionResource(R.dimen.padding_small),
                    start = dimensionResource(R.dimen.padding_medium),
                    bottom = dimensionResource(R.dimen.padding_small),
                )
        ) {
            Text(
                text = stringResource(timeRecommendation.headText),
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
            Text(
                text = stringResource(timeRecommendation.descriptionText),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun StatsGrid(
    resultPower: Double,
    resultVoltage: Double,
    resultCurrent: Double,
    resultEquiv: Double
) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        // First Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                icon = Icons.Default.FlashOn,
                title = stringResource(R.string.voltage_v),
                value = resultVoltage,
                backgroundColor = Color(0xFFFF7043), // Orange
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.ShowChart,
                title = stringResource(R.string.current_ma),
                value = resultCurrent,
                backgroundColor = Color(0xFFAB47BC), // Purple
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Second Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                icon = Icons.Default.Bolt,
                title = stringResource(R.string.power_mw),
                value = resultPower,
                backgroundColor = Color(0xFF42A5F5), // Blue
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.VolumeUp,
                title = stringResource(R.string.equiv_db_1v),
                value = resultEquiv,
                backgroundColor = Color(0xFF66BB6A), // Green
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: Double,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_small)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title, color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "%.2f".format(value),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EditNumberField(
    @StringRes label: Int,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {

    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(stringResource(label)) },
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.padding_huge))
            .padding(
                start = dimensionResource(R.dimen.padding_medium),
                end = dimensionResource(R.dimen.padding_medium)
            ),
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadphoneTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HeadphonesPowerCalculatorTheme {
        HapApp()
    }
}