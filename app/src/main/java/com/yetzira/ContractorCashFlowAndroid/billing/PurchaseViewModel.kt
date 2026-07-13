package com.yetzira.ContractorCashFlowAndroid.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val purchaseManager: PurchaseManager
) : ViewModel() {

    val isProUser = purchaseManager.isProUser
    val devProOverride = purchaseManager.devProOverride
    val products = purchaseManager.products
    val isLoading = purchaseManager.isLoading
    val isPurchasing = purchaseManager.isPurchasing
    val errorMessage = purchaseManager.errorMessage
    val statusMessage = purchaseManager.statusMessage
    val activePurchase = purchaseManager.activePurchase
    val isRestoring = purchaseManager.isRestoring
    val hasPendingPurchase = purchaseManager.hasPendingPurchase

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, basePlanId: String) {
        purchaseManager.launchPurchaseFlow(activity, productDetails, basePlanId)
    }

    fun restorePurchases() {
        viewModelScope.launch {
            purchaseManager.restorePurchases()
        }
    }

    fun refreshEntitlements() {
        viewModelScope.launch {
            purchaseManager.checkCurrentEntitlements()
        }
    }

    fun reloadProducts() {
        viewModelScope.launch {
            purchaseManager.loadProducts()
        }
    }

    fun reconnect() {
        purchaseManager.connectAndLoad()
    }

    fun clearError() {
        purchaseManager.clearError()
    }

    fun clearStatusMessage() {
        purchaseManager.clearStatusMessage()
    }

    fun toggleDevProOverride() {
        purchaseManager.toggleDevProOverride()
    }

    fun openManageSubscriptions(context: Context) {
        purchaseManager.openManageSubscriptions(context)
    }
}
