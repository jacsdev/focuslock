package com.developermind.focuslock.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.developermind.focuslock.R
import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.model.BatteryState

private val RingTrack = Color(0xFF2A2A2A)

private fun ringColor(theme: AppTheme, percentage: Int): Color = when (theme) {
    AppTheme.DYNAMIC -> when {
        percentage > 50 -> Color(0xFF4CAF50)
        percentage > 20 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    AppTheme.OCEAN -> Color(0xFF2196F3)
    AppTheme.AURORA -> Color(0xFFAB47BC)
    AppTheme.ARCTIC -> Color(0xFFE0E0E0)
}

@Composable
fun BatteryRing(
    battery: BatteryState,
    theme: AppTheme = AppTheme.DYNAMIC,
    modifier: Modifier = Modifier,
    ringSize: Dp = 240.dp,
    strokeWidth: Dp = 24.dp,
) {
    val targetProgress = battery.percentage / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1_000, easing = FastOutSlowInEasing),
        label = "batteryProgress",
    )

    val color = ringColor(theme, battery.percentage)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(ringSize),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcTopLeft = Offset(inset, inset)
            val arcSize = Size(size.width - strokePx, size.height - strokePx)

            drawArc(
                color = RingTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (animatedProgress > 0f) {
                drawCircle(color = color.copy(alpha = 0.12f), radius = size.minDimension / 2f)
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${battery.percentage}%",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(4.dp))
            ChargingLabel(battery = battery)
        }
    }
}

@Composable
private fun ChargingLabel(battery: BatteryState) {
    val label = when {
        battery.isFull -> stringResource(R.string.battery_full)
        battery.isCharging -> stringResource(R.string.battery_charging)
        battery.isLow -> stringResource(R.string.battery_low)
        else -> stringResource(R.string.battery_disconnected)
    }

    val alpha = if (battery.isCharging) {
        val transition = rememberInfiniteTransition(label = "chargingPulse")
        transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "chargingAlpha",
        ).value
    } else {
        1f
    }

    Text(
        text = label,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFAAAAAA).copy(alpha = alpha),
    )
}
