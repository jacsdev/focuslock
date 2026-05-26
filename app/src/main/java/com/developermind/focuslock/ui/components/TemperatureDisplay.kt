package com.developermind.focuslock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.developermind.focuslock.R
import com.developermind.focuslock.data.model.WeatherCondition
import kotlin.math.roundToInt

@Composable
fun TemperatureDisplay(
    city: String,
    temperature: Float? = null,
    isStale: Boolean = false,
    condition: WeatherCondition? = null,
) {
    val tempText = temperature?.let { "${it.roundToInt()} °C" } ?: "-- °C"
    val accentColor = if (isStale) Color(0xFFAAAAAA) else Color.White

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (condition != null) {
            WeatherIcon(
                condition = condition,
                size = 96.dp,
                tint = accentColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = tempText,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
        )
        Text(
            text = if (city.isNotBlank()) city else stringResource(R.string.pref_city_hint),
            fontSize = 22.sp,
            color = if (city.isNotBlank()) Color(0xFFAAAAAA) else Color(0xFF444444),
        )
    }
}
