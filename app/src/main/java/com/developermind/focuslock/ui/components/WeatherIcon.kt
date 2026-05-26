package com.developermind.focuslock.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.developermind.focuslock.R
import com.developermind.focuslock.data.model.WeatherCondition

@DrawableRes
private fun WeatherCondition.drawableRes(): Int = when (this) {
    WeatherCondition.CLEAR_DAY -> R.drawable.ic_weather_clear_day
    WeatherCondition.CLEAR_NIGHT -> R.drawable.ic_weather_clear_night
    WeatherCondition.PARTLY_CLOUDY_DAY -> R.drawable.ic_weather_partly_cloudy_day
    WeatherCondition.PARTLY_CLOUDY_NIGHT -> R.drawable.ic_weather_partly_cloudy_night
    WeatherCondition.CLOUDY -> R.drawable.ic_weather_cloudy
    WeatherCondition.RAIN -> R.drawable.ic_weather_rain
    WeatherCondition.SNOW -> R.drawable.ic_weather_snow
    WeatherCondition.THUNDERSTORM -> R.drawable.ic_weather_thunderstorm
}

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    size: Dp,
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(condition.drawableRes()),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size),
    )
}
