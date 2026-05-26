package com.developermind.focuslock.ui.admin

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.developermind.focuslock.R
import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.model.WeatherCondition
import com.developermind.focuslock.ui.components.WeatherIcon
import com.developermind.focuslock.util.LocaleManager
import com.developermind.focuslock.util.LanguageOption
import kotlin.math.roundToInt

private val BackgroundColor = Color(0xFF0D0D0D)
private val CardColor = Color(0xFF1A1A1A)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF888888)
private val ColorGranted = Color(0xFF4CAF50)
private val ColorDenied = Color(0xFFF44336)

@Composable
fun AdminScreen(
    uiState: AdminUiState,
    onSetTheme: (AppTheme) -> Unit,
    onSetLanguage: (String) -> Unit,
    onRefresh: () -> Unit,
    onSetShowBattery: (Boolean) -> Unit = {},
    onSetShowTemperature: (Boolean) -> Unit = {},
    onSetWeatherCity: (String) -> Unit = {},
    onClearWeatherCity: () -> Unit = {},
    onRequestDeleteCity: () -> Unit = {},
    onCancelDeleteCity: () -> Unit = {},
    onSupportClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        AppHeader()

        Spacer(modifier = Modifier.height(36.dp))
        SectionHeader(stringResource(R.string.section_permissions))
        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = stringResource(R.string.perm_accessibility_title),
            description = stringResource(R.string.perm_accessibility_desc),
            isGranted = uiState.isAccessibilityServiceEnabled,
            grantedLabel = stringResource(R.string.perm_accessibility_status_ok),
            deniedLabel = stringResource(R.string.perm_accessibility_status_restricted),
            actionLabel = if (uiState.isAccessibilityServiceEnabled)
                stringResource(R.string.perm_accessibility_action_change)
            else
                stringResource(R.string.perm_accessibility_action_enable),
            onAction = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermissionCard(
            title = stringResource(R.string.perm_battery_title),
            description = stringResource(R.string.perm_battery_desc),
            isGranted = uiState.isBatteryOptimizationIgnored,
            grantedLabel = stringResource(R.string.perm_battery_status_ok),
            deniedLabel = stringResource(R.string.perm_battery_status_restricted),
            actionLabel = if (uiState.isBatteryOptimizationIgnored)
                stringResource(R.string.perm_battery_action_change)
            else
                stringResource(R.string.perm_battery_action_disable),
            onAction = {
                try {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}"),
                        )
                    )
                } catch (_: Exception) {
                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = stringResource(R.string.perm_autostart_title),
            description = stringResource(R.string.perm_autostart_desc),
            actionLabel = stringResource(R.string.perm_autostart_action),
            onAction = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}"),
                    )
                )
            },
        )

        Spacer(modifier = Modifier.height(32.dp))
        SectionHeader(stringResource(R.string.section_display))
        Spacer(modifier = Modifier.height(12.dp))

        SwitchCard(
            title = stringResource(R.string.pref_show_battery_title),
            description = stringResource(R.string.pref_show_battery_desc),
            checked = uiState.showBattery,
            onCheckedChange = onSetShowBattery,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SwitchCard(
            title = stringResource(R.string.pref_show_temperature_title),
            description = stringResource(R.string.pref_show_temperature_desc),
            checked = uiState.showTemperature,
            onCheckedChange = onSetShowTemperature,
        )

        if (uiState.showTemperature) {
            Spacer(modifier = Modifier.height(10.dp))
            CityInputCard(
                currentCity = uiState.weatherCity,
                cityEditState = uiState.cityEditState,
                cachedTemperature = uiState.cachedTemperature,
                temperatureIsStale = uiState.temperatureIsStale,
                weatherCondition = uiState.weatherCondition,
                onSave = onSetWeatherCity,
                onDelete = onClearWeatherCity,
                onRequestDelete = onRequestDeleteCity,
                onCancelDelete = onCancelDeleteCity,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        SectionHeader(stringResource(R.string.section_theme_color))
        Spacer(modifier = Modifier.height(16.dp))

        ThemeSelector(
            selectedTheme = uiState.selectedTheme,
            onThemeSelected = onSetTheme,
        )

        Spacer(modifier = Modifier.height(32.dp))
        SectionHeader(stringResource(R.string.section_language))
        Spacer(modifier = Modifier.height(16.dp))

        LanguageSelector(
            selectedTag = uiState.selectedLanguageTag,
            languages = LocaleManager.supportedLanguages,
            onLanguageSelected = onSetLanguage,
        )

        Spacer(modifier = Modifier.height(40.dp))
        SectionHeader(stringResource(R.string.section_support))
        Spacer(modifier = Modifier.height(12.dp))
        SupportCard(onClick = onSupportClick)
        Spacer(modifier = Modifier.height(48.dp))
        AppFooter()
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SupportCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.support_card_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.support_card_desc),
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "›",
            fontSize = 20.sp,
            color = Color(0xFF64B5F6),
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
private fun AppFooter() {
    Text(
        text = "DeveloperMind Solutions",
        fontSize = 12.sp,
        color = Color.White,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AppHeader() {
    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }
    val iconPainter = remember {
        val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)!!
        val size = 192
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(Canvas(bmp))
        BitmapPainter(bmp.asImageBitmap())
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
        )
        Column {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = stringResource(R.string.admin_subtitle_settings),
                fontSize = 13.sp,
                color = TextSecondary,
            )
            if (versionName.isNotEmpty()) {
                Text(
                    text = "v$versionName",
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        color = TextSecondary,
    )
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    grantedLabel: String,
    deniedLabel: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            StatusChip(isGranted = isGranted, label = if (isGranted) grantedLabel else deniedLabel)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = if (isGranted) TextSecondary else Color(0xFF64B5F6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, description: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .padding(16.dp),
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(isGranted: Boolean, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(if (isGranted) ColorGranted else ColorDenied, CircleShape)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isGranted) ColorGranted else ColorDenied,
        )
    }
}

@Composable
private fun SwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun CityInputCard(
    currentCity: String,
    cityEditState: CityEditState,
    cachedTemperature: Float?,
    temperatureIsStale: Boolean,
    weatherCondition: WeatherCondition?,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onRequestDelete: () -> Unit,
    onCancelDelete: () -> Unit,
) {
    var isEditing by rememberSaveable(currentCity) { mutableStateOf(currentCity.isBlank()) }
    var localCity by rememberSaveable(currentCity) { mutableStateOf(currentCity) }

    LaunchedEffect(cityEditState) {
        if (cityEditState == CityEditState.Success) isEditing = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor)
            .padding(16.dp),
    ) {
        if (currentCity.isNotBlank() && !isEditing) {
            // ── View mode ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: city name + temperature stacked vertically
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                ) {
                    Text(
                        text = currentCity,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (cachedTemperature != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (weatherCondition != null) {
                                WeatherIcon(
                                    condition = weatherCondition,
                                    size = 16.dp,
                                    tint = if (temperatureIsStale) Color(0xFF555555) else TextSecondary,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${cachedTemperature.roundToInt()} °C",
                                fontSize = 12.sp,
                                color = if (temperatureIsStale) Color(0xFF555555) else TextSecondary,
                                maxLines = 1,
                            )
                        }
                    }
                }
                // Right: action buttons
                if (cityEditState == CityEditState.ConfirmDelete) {
                    Row {
                        TextButton(onClick = onCancelDelete) {
                            Text(
                                text = stringResource(R.string.city_action_cancel),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                        }
                        TextButton(onClick = onDelete) {
                            Text(
                                text = stringResource(R.string.city_action_confirm),
                                color = Color(0xFFEF5350),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                } else {
                    Row {
                        TextButton(onClick = { isEditing = true }) {
                            Text(
                                text = stringResource(R.string.city_action_edit),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                        }
                        TextButton(onClick = onRequestDelete) {
                            Text(
                                text = stringResource(R.string.city_action_delete),
                                color = Color(0xFFEF5350),
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            if (cityEditState == CityEditState.ConfirmDelete) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.city_delete_confirm),
                    fontSize = 12.sp,
                    color = Color(0xFFEF5350),
                )
            }

            if (cityEditState == CityEditState.Success) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("✓", color = ColorGranted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = stringResource(R.string.city_saved),
                        color = ColorGranted,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
            }
        } else {
            // ── Edit / input mode ─────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedTextField(
                value = localCity,
                onValueChange = { localCity = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = cityEditState != CityEditState.Saving,
                placeholder = {
                    Text(
                        text = stringResource(R.string.pref_city_hint),
                        color = TextSecondary,
                        fontSize = 14.sp,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledTextColor = TextSecondary,
                    focusedBorderColor = Color(0xFF64B5F6),
                    unfocusedBorderColor = Color(0xFF444444),
                    disabledBorderColor = Color(0xFF333333),
                    cursorColor = Color(0xFF64B5F6),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (cityEditState) {
                    CityEditState.Saving -> {
                        if (currentCity.isNotBlank()) {
                            TextButton(onClick = {}, enabled = false) {
                                Text(
                                    text = stringResource(R.string.city_action_cancel),
                                    color = Color(0xFF444444),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically),
                            strokeWidth = 2.dp,
                            color = Color(0xFF64B5F6),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    CityEditState.Error -> {
                        Text(
                            text = stringResource(R.string.city_save_error),
                            color = ColorDenied,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { onSave(localCity.trim()) },
                            enabled = localCity.isNotBlank(),
                        ) {
                            Text(
                                text = stringResource(R.string.pref_city_save),
                                color = Color(0xFF64B5F6),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                    else -> {
                        if (currentCity.isNotBlank()) {
                            TextButton(onClick = { isEditing = false; localCity = currentCity }) {
                                Text(
                                    text = stringResource(R.string.city_action_cancel),
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                        TextButton(
                            onClick = { onSave(localCity.trim()) },
                            enabled = localCity.isNotBlank(),
                        ) {
                            Text(
                                text = stringResource(R.string.pref_city_save),
                                color = if (localCity.isNotBlank()) Color(0xFF64B5F6) else Color(0xFF555555),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(selectedTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AppTheme.entries.forEach { theme ->
            ThemeOption(
                theme = theme,
                isSelected = theme == selectedTheme,
                onClick = { onThemeSelected(theme) },
            )
        }
    }
}

@Composable
private fun ThemeOption(theme: AppTheme, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(theme.previewColor.copy(alpha = 0.15f))
                .then(
                    if (isSelected) Modifier.border(2.dp, theme.previewColor, CircleShape)
                    else Modifier
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(theme.previewColor, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(theme.labelRes),
            fontSize = 11.sp,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LanguageSelector(
    selectedTag: String,
    languages: List<LanguageOption>,
    onLanguageSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardColor),
    ) {
        languages.forEachIndexed { index, option ->
            val isSelected = option.tag == selectedTag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(option.tag) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = option.englishName,
                    fontSize = 14.sp,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (option.nativeName != null) {
                        Text(
                            text = option.nativeName,
                            fontSize = 13.sp,
                            color = if (isSelected) Color(0xFF888888) else Color(0xFF555555),
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isSelected) ColorGranted else Color.Transparent,
                                shape = CircleShape,
                            )
                    )
                }
            }
            if (index < languages.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFF2A2A2A))
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun AdminScreenPreview() {
    AdminScreen(
        uiState = AdminUiState(
            isBatteryOptimizationIgnored = true,
            isAccessibilityServiceEnabled = false,
            selectedTheme = AppTheme.OCEAN,
        ),
        onSetTheme = {},
        onSetLanguage = {},
        onRefresh = {},
    )
}
