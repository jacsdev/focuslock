package com.developermind.focuslock.ui.donation

import com.developermind.focuslock.data.billing.model.DonationProduct

sealed class DonationUiState {
    data object Loading : DonationUiState()
    data class Ready(val products: List<DonationProduct>) : DonationUiState()
    data class Purchasing(val products: List<DonationProduct>) : DonationUiState()
    data object Success : DonationUiState()
    data object Pending : DonationUiState()
    data class Error(val canRetry: Boolean) : DonationUiState()
}
