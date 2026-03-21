package com.yetzira.ContractorCashFlowAndroid.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.billing.BillingActionState
import com.yetzira.ContractorCashFlowAndroid.billing.BillingRepositoryContract
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val billingRepository: BillingRepositoryContract
) : ViewModel() {
    val uiState: StateFlow<PaywallUiState> = combine(
        billingRepository.productState,
        billingRepository.entitlementState,
        billingRepository.actionState
    ) { product, entitlement, action ->
        val processing = action == BillingActionState.Loading || action == BillingActionState.LaunchingPurchase
        PaywallUiState(
            isLoadingProduct = product.isLoading || entitlement.isLoading,
            isProcessingAction = processing,
            isPro = entitlement.isPro,
            productTitle = product.title,
            productDescription = product.description,
            priceText = product.priceText,
            isProductAvailable = product.isAvailable,
            status = when (action) {
                BillingActionState.Idle -> PaywallStatus.IDLE
                BillingActionState.Loading,
                BillingActionState.LaunchingPurchase -> PaywallStatus.LOADING
                BillingActionState.Purchased -> PaywallStatus.PURCHASED
                BillingActionState.Restored -> PaywallStatus.RESTORED
                BillingActionState.UserCancelled -> PaywallStatus.USER_CANCELLED
                is BillingActionState.Pending -> PaywallStatus.PENDING
                is BillingActionState.Error -> PaywallStatus.ERROR
            },
            statusDetail = when (action) {
                is BillingActionState.Pending -> action.message
                is BillingActionState.Error -> action.message
                else -> product.errorMessage
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaywallUiState()
    )

    init {
        viewModelScope.launch {
            billingRepository.refresh()
        }
    }

    fun purchase(activity: Activity) {
        viewModelScope.launch {
            billingRepository.launchPurchase(activity)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingRepository.refresh()
        }
    }

    fun clearStatus() {
        billingRepository.clearActionState()
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.close()
    }
}

