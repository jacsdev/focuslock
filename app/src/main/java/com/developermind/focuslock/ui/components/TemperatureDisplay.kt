package com.developermind.focuslock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.developermind.focuslock.R
import kotlin.math.roundToInt

@Composable
fun TemperatureDisplay(
    city: String,
    temperature: Float? = null,
    isStale: Boolean = false,
) {
    val tempText = temperature?.let { "${it.roundToInt()} °C" } ?: "-- °C"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = tempText,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = if (isStale) Color(0xFFAAAAAA) else Color.White,
        )
        Text(
            text = if (city.isNotBlank()) city else stringResource(R.string.pref_city_hint),
            fontSize = 22.sp,
            color = if (city.isNotBlank()) Color(0xFFAAAAAA) else Color(0xFF444444),
        )
    }
}
