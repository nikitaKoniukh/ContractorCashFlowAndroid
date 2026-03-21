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
    val products = purchaseManager.products
    val isLoading = purchaseManager.isLoading
    val isPurchasing = purchaseManager.isPurchasing
    val errorMessage = purchaseManager.errorMessage
    val activePurchase = purchaseManager.activePurchase

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

    fun clearError() {
        purchaseManager.clearError()
    }

    fun openManageSubscriptions(context: Context) {
        purchaseManager.openManageSubscriptions(context)
    }
}

