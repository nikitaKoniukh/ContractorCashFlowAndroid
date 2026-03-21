package com.yetzira.ContractorCashFlowAndroid.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class BillingProductState(
    val isLoading: Boolean = true,
    val title: String? = null,
    val description: String? = null,
    val priceText: String? = null,
    val isAvailable: Boolean = false,
    val errorMessage: String? = null
)

data class BillingEntitlementState(
    val isLoading: Boolean = true,
    val isPro: Boolean = false,
    val planName: String? = null,
    val renewalDate: Long? = null,
    val autoRenewing: Boolean = false,
    val purchaseToken: String? = null
)

sealed interface BillingActionState {
    data object Idle : BillingActionState
    data object Loading : BillingActionState
    data object LaunchingPurchase : BillingActionState
    data object Purchased : BillingActionState
    data object Restored : BillingActionState
    data object UserCancelled : BillingActionState
    data class Pending(val message: String) : BillingActionState
    data class Error(val message: String) : BillingActionState
}

interface BillingRepositoryContract {
    val productState: StateFlow<BillingProductState>
    val entitlementState: StateFlow<BillingEntitlementState>
    val actionState: StateFlow<BillingActionState>

    suspend fun refresh(): Result<Unit>
    suspend fun launchPurchase(activity: Activity): Result<Unit>
    fun clearActionState()
    fun close()
}

