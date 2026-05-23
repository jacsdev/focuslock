package com.developermind.focuslock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.developermind.focuslock.service.ScreenMonitorService
import com.developermind.focuslock.ui.admin.AdminScreen
import com.developermind.focuslock.ui.admin.AdminViewModel
import com.developermind.focuslock.ui.theme.FocusLockTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        startScreenMonitorService()
        viewModel.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionOrStartService()

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            FocusLockTheme {
                AdminScreen(
                    uiState = uiState.value,
                    onSetTheme = viewModel::setTheme,
                    onSetLanguage = viewModel::setLanguage,
                    onRefresh = viewModel::refreshPermissions,
                )
            }
        }
    }

    private fun requestNotificationPermissionOrStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startScreenMonitorService()
        }
    }

    private fun startScreenMonitorService() {
        startForegroundService(Intent(this, ScreenMonitorService::class.java))
    }
}
