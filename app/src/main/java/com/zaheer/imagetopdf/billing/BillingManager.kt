package com.zaheer.imagetopdf.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
    
    private val _isPro = MutableStateFlow(prefs.getBoolean("is_pro", false))
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()
    
    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    
    companion object {
        private const val PRODUCT_ID = "imagetopdf_pro_unlock"
        private const val PREF_IS_PRO = "is_pro"
    }
    
    fun startConnection() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryPurchases()
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request
            }
        })
    }
    
    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = productDetailsList.firstOrNull()
            }
        }
    }
    
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                        updateProStatus(true)
                    }
                }
            }
        }
    }
    
    suspend fun launchPurchaseFlow(activity: Activity): Boolean {
        val details = productDetails ?: return false
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        return billingResult?.responseCode == BillingClient.BillingResponseCode.OK
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            updateProStatus(true)
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                updateProStatus(true)
            }
        }
    }
    
    fun restorePurchases() {
        queryPurchases()
    }
    
    private fun updateProStatus(isPro: Boolean) {
        _isPro.value = isPro
        prefs.edit().putBoolean(PREF_IS_PRO, isPro).apply()
    }
    
    fun endConnection() {
        billingClient?.endConnection()
    }
}
