package com.developermind.focuslock.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * OEM-specific settings deep links with graceful fallbacks.
 * Each function tries the vendor-specific screen first, then falls back
 * to the standard Android settings equivalent.
 */
object OemDeepLinks {

    val isMiui: Boolean
        get() = Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)

    /**
     * Opens the Autostart manager for the app.
     * On MIUI this is inside Security Center; on stock Android it falls back
     * to the app's system settings page.
     */
    fun openAutostart(context: Context) {
        val miuiAutostart = Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity",
            )
        }
        if (!tryStart(context, miuiAutostart)) {
            tryStart(
                context,
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}"),
                )
            )
        }
    }

    /**
     * Opens MIUI's per-app power management screen so the user can set
     * FocusLock to "No restrictions". Falls back to app details on other OEMs.
     */
    fun openMiuiBatterySaver(context: Context) {
        val appName = context.applicationInfo
            .loadLabel(context.packageManager).toString()

        // Powerkeeper direct screen (MIUI 12+)
        val powerkeeper = Intent().apply {
            component = ComponentName(
                "com.miui.powerkeeper",
                "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity",
            )
            putExtra("package_name", context.packageName)
            putExtra("package_label", appName)
        }
        if (!tryStart(context, powerkeeper)) {
            tryStart(
                context,
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}"),
                )
            )
        }
    }

    private fun tryStart(context: Context, intent: Intent): Boolean =
        try { context.startActivity(intent); true } catch (_: Exception) { false }
}
