package com.yetzira.ContractorCashFlowAndroid.ui.paywall

enum class PaywallStatus {
    IDLE,
    LOADING,
    PURCHASED,
    RESTORED,
    PENDING,
    USER_CANCELLED,
    ERROR
}

data class PaywallUiState(
    val isLoadingProduct: Boolean = true,
    val isProcessingAction: Boolean = false,
    val isPro: Boolean = false,
    val productTitle: String? = null,
    val productDescription: String? = null,
    val priceText: String? = null,
    val isProductAvailable: Boolean = false,
    val status: PaywallStatus = PaywallStatus.IDLE,
    val statusDetail: String? = null
) {
    val canPurchase: Boolean
        get() = !isPro && isProductAvailable && !isProcessingAction
}

