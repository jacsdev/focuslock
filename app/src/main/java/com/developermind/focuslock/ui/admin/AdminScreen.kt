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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.developermind.focuslock.R
import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.util.LocaleManager
import com.developermind.focuslock.util.LanguageOption

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
    }
}

@Composable
private fun AppHeader() {
    val context = LocalContext.current
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
