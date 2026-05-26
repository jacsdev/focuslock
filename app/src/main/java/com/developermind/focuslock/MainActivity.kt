package com.developermind.focuslock

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.developermind.focuslock.ui.admin.AdminScreen
import com.developermind.focuslock.ui.admin.AdminViewModel
import com.developermind.focuslock.ui.donation.DonationScreen
import com.developermind.focuslock.ui.theme.FocusLockTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            FocusLockTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "admin") {
                    composable("admin") {
                        AdminScreen(
                            uiState = uiState.value,
                            onSetTheme = viewModel::setTheme,
                            onSetLanguage = viewModel::setLanguage,
                            onRefresh = viewModel::refreshPermissions,
                            onSetShowBattery = viewModel::setShowBattery,
                            onSetShowTemperature = viewModel::setShowTemperature,
                            onSetWeatherCity = viewModel::setWeatherCity,
                            onClearWeatherCity = viewModel::clearWeatherCity,
                            onRequestDeleteCity = viewModel::requestDeleteCity,
                            onCancelDeleteCity = viewModel::cancelDeleteCity,
                            onSupportClick = { navController.navigate("donation") },
                        )
                    }
                    composable("donation") {
                        DonationScreen(
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
