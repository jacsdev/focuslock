package com.developermind.focuslock.data.billing.model

import com.android.billingclient.api.Purchase

sealed class PurchaseResult {
    data class Success(val purchase: Purchase) : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data object Pending : PurchaseResult()
    data class Error(val responseCode: Int) : PurchaseResult()
}
