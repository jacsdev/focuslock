package com.developermind.focuslock.ui.donation

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.developermind.focuslock.data.billing.DonationRepository
import com.developermind.focuslock.data.billing.model.DonationProduct
import com.developermind.focuslock.data.billing.model.PurchaseResult
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DonationViewModel(
    private val repository: DonationRepository,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DonationUiState>(DonationUiState.Loading)
    val uiState: StateFlow<DonationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadProducts() }
        viewModelScope.launch { collectPurchaseResults() }
    }

    fun onScreenViewed() {
        analytics.logEvent("donation_screen_opened", null)
    }

    fun onPurchaseClick(product: DonationProduct, activity: Activity) {
        analytics.logEvent("donation_purchase_initiated", Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, product.id)
        })
        val launched = repository.launchPurchase(activity, product)
        if (launched) {
            _uiState.value = DonationUiState.Purchasing(currentProductList())
        } else {
            _uiState.value = DonationUiState.Error(canRetry = false)
            logError("launch_failed")
        }
    }

    fun onRetry() {
        _uiState.value = DonationUiState.Loading
        viewModelScope.launch { loadProducts() }
    }

    private suspend fun loadProducts() {
        val success = repository.loadProducts()
        _uiState.value = if (success) {
            DonationUiState.Ready(repository.products.value)
        } else {
            DonationUiState.Error(canRetry = true)
        }
    }

    private suspend fun collectPurchaseResults() {
        repository.purchaseResults.collect { result ->
            when (result) {
                is PurchaseResult.Success -> {
                    viewModelScope.launch { repository.handlePurchase(result.purchase) }
                    analytics.logEvent("donation_purchase_success", Bundle().apply {
                        putString(FirebaseAnalytics.Param.ITEM_ID, result.purchase.products.firstOrNull())
                    })
                    _uiState.value = DonationUiState.Success
                }
                is PurchaseResult.Cancelled -> {
                    analytics.logEvent("donation_purchase_cancelled", null)
                    _uiState.value = DonationUiState.Ready(currentProductList())
                }
                is PurchaseResult.Pending -> {
                    _uiState.value = DonationUiState.Pending
                }
                is PurchaseResult.Error -> {
                    logError(result.responseCode.toString())
                    _uiState.value = DonationUiState.Error(canRetry = false)
                }
            }
        }
    }

    private fun currentProductList() = when (val s = _uiState.value) {
        is DonationUiState.Ready -> s.products
        is DonationUiState.Purchasing -> s.products
        else -> repository.products.value
    }

    private fun logError(code: String) {
        analytics.logEvent("donation_purchase_error", Bundle().apply {
            putString("error_code", code)
        })
    }

    class Factory(
        private val repository: DonationRepository,
        private val analytics: FirebaseAnalytics,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DonationViewModel(repository, analytics) as T
        }
    }
}
