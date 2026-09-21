package com.suyash.mockcivilaviationexam.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_ID_YEARLY = "aviation_exam_pro_yearly"
    }

    private var billingClient: BillingClient? = null

    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    data class SubscriptionState(
        val isSubscribed: Boolean = false,
        val isLoading: Boolean = true,
        val expiryTimeMillis: Long = 0,
        val error: String? = null
    )

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    querySubscriptionStatus()
                    queryProductDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = "Billing setup failed"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = queryProductDetailsResult.productDetailsList.firstOrNull()
                _productDetails.value = details
                if (details != null) {
                    Log.d(TAG, "Product details loaded: ${details.name}")
                } else {
                    Log.w(TAG, "No product details found for $PRODUCT_ID_YEARLY")
                }
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    fun querySubscriptionStatus() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activeSub = purchases.firstOrNull { purchase ->
                    purchase.products.contains(PRODUCT_ID_YEARLY) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                _subscriptionState.value = SubscriptionState(
                    isSubscribed = activeSub != null,
                    isLoading = false,
                    error = null
                )

                // Acknowledge if needed
                activeSub?.let { purchase ->
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }

                Log.d(TAG, "Subscription status: ${if (activeSub != null) "ACTIVE" else "INACTIVE"}")
            } else {
                Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity): Boolean {
        val details = _productDetails.value
        if (details == null) {
            Log.e(TAG, "Product details not available")
            _subscriptionState.value = _subscriptionState.value.copy(
                error = "Subscription not available. Please try again later."
            )
            return false
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            Log.e(TAG, "No offer token available")
            _subscriptionState.value = _subscriptionState.value.copy(
                error = "Subscription offer not available."
            )
            return false
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
        return result?.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchase(purchase)
                        _subscriptionState.value = SubscriptionState(
                            isSubscribed = true,
                            isLoading = false,
                            error = null
                        )
                        Log.d(TAG, "Purchase successful")
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Purchase cancelled by user")
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _subscriptionState.value = _subscriptionState.value.copy(
                    error = "Purchase failed. Please try again."
                )
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
            } else {
                Log.e(TAG, "Acknowledgement failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun clearError() {
        _subscriptionState.value = _subscriptionState.value.copy(error = null)
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
