package com.developermind.focuslock

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.developermind.focuslock.ui.admin.AdminScreen
import com.developermind.focuslock.ui.admin.AdminViewModel
import com.developermind.focuslock.ui.theme.FocusLockTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
