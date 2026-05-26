package com.developermind.focuslock.data.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.developermind.focuslock.data.billing.model.DonationProduct
import com.developermind.focuslock.data.billing.model.PurchaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DonationRepository(
    private val billingClientWrapper: BillingClientWrapper,
) {
    val purchaseResults: SharedFlow<PurchaseResult> = billingClientWrapper.purchaseResults

    private val _products = MutableStateFlow<List<DonationProduct>>(emptyList())
    val products: StateFlow<List<DonationProduct>> = _products.asStateFlow()

    private val productDetailsCache = mutableMapOf<String, ProductDetails>()

    suspend fun loadProducts(): Boolean {
        val details = billingClientWrapper.queryProductDetails(PRODUCT_IDS)
        productDetailsCache.clear()
        details.forEach { productDetailsCache[it.productId] = it }
        _products.value = PRODUCT_IDS
            .mapNotNull { id -> details.find { it.productId == id } }
            .map { it.toDonationProduct() }
        return details.isNotEmpty()
    }

    fun launchPurchase(activity: Activity, product: DonationProduct): Boolean {
        val productDetails = productDetailsCache[product.id] ?: return false
        val result = billingClientWrapper.launchBillingFlow(activity, productDetails)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            billingClientWrapper.consumePurchase(purchase.purchaseToken)
        }
    }

    private fun ProductDetails.toDonationProduct() = DonationProduct(
        id = productId,
        title = title,
        description = description,
        formattedPrice = oneTimePurchaseOfferDetails?.formattedPrice ?: "",
        emoji = PRODUCT_EMOJIS[productId] ?: "💙",
    )

    companion object {
        val PRODUCT_IDS = listOf(
            "focuslock_donation_small",
            "focuslock_donation_medium",
            "focuslock_donation_large",
        )
        private val PRODUCT_EMOJIS = mapOf(
            "focuslock_donation_small" to "☕",
            "focuslock_donation_medium" to "🍕",
            "focuslock_donation_large" to "⭐",
        )
    }
}
