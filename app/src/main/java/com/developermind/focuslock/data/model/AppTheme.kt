package com.developermind.focuslock.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.developermind.focuslock.R

enum class AppTheme(
    @StringRes val labelRes: Int,
    val previewColor: Color,
) {
    DYNAMIC(
        labelRes = R.string.theme_dynamic,
        previewColor = Color(0xFF4CAF50),
    ),
    OCEAN(
        labelRes = R.string.theme_ocean,
        previewColor = Color(0xFF2196F3),
    ),
    AURORA(
        labelRes = R.string.theme_aurora,
        previewColor = Color(0xFFAB47BC),
    ),
    ARCTIC(
        labelRes = R.string.theme_arctic,
        previewColor = Color(0xFFE0E0E0),
    ),
}
