package com.developermind.focuslock.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.developermind.focuslock.data.model.BatteryState
import com.developermind.focuslock.ui.components.BatteryRing
import com.developermind.focuslock.ui.components.TimeDisplay

@Composable
fun OverlayScreen(uiState: OverlayUiState, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TimeDisplay(time = uiState.time, date = uiState.date)
        Spacer(modifier = Modifier.height(48.dp))
        BatteryRing(battery = uiState.battery, theme = uiState.theme)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun OverlayScreenPreview() {
    OverlayScreen(
        uiState = OverlayUiState(
            time = "22:45",
            date = "viernes, 23 de mayo",
            battery = BatteryState(percentage = 72, isCharging = false),
        ),
        onDismiss = {},
    )
}
