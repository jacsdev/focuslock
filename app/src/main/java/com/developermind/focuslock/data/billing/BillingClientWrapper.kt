package com.developermind.focuslock.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.developermind.focuslock.data.billing.model.PurchaseResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BillingClientWrapper(context: Context) {

    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 1)
    val purchaseResults: SharedFlow<PurchaseResult> = _purchaseResults.asSharedFlow()

    val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases ->
            onPurchasesUpdated(result, purchases)
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when {
            result.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                purchases.forEach { purchase ->
                    val event = when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> PurchaseResult.Success(purchase)
                        Purchase.PurchaseState.PENDING -> PurchaseResult.Pending
                        else -> return@forEach
                    }
                    _purchaseResults.tryEmit(event)
                }
            }
            result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED ->
                _purchaseResults.tryEmit(PurchaseResult.Cancelled)
            else ->
                _purchaseResults.tryEmit(PurchaseResult.Error(result.responseCode))
        }
    }

    suspend fun ensureConnected(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) {
                        continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }
                override fun onBillingServiceDisconnected() {
                    if (continuation.isActive) continuation.resume(false)
                }
            })
        }
    }

    suspend fun queryProductDetails(productIds: List<String>): List<ProductDetails> {
        if (!ensureConnected()) return emptyList()
        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(params) { result, details ->
                if (continuation.isActive) {
                    continuation.resume(
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) details
                        else emptyList()
                    )
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): BillingResult {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()
        return client.launchBillingFlow(activity, params)
    }

    suspend fun consumePurchase(purchaseToken: String): Boolean {
        if (!ensureConnected()) return false
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        return suspendCancellableCoroutine { continuation ->
            client.consumeAsync(params) { result, _ ->
                if (continuation.isActive) {
                    continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }
            }
        }
    }

    @Suppress("unused")
    suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        if (!ensureConnected()) return false
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        return suspendCancellableCoroutine { continuation ->
            client.acknowledgePurchase(params) { result ->
                if (continuation.isActive) {
                    continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }
            }
        }
    }
}
