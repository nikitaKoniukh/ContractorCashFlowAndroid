package com.yetzira.ContractorCashFlowAndroid.ui.settings

import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption

enum class CloudSyncState {
    IDLE,
    SYNCING,
    DONE,
    FAILED
}

data class SubscriptionUiState(
    val isPro: Boolean = false,
    val planName: String = "Free Plan",
    val renewalDate: Long? = null
)

data class SettingsUiState(
    val selectedLanguage: AppLanguageOption = AppLanguageOption.HEBREW,
    val selectedCurrency: CurrencyOption = CurrencyOption.ILS,
    val invoiceRemindersEnabled: Boolean = true,
    val overdueAlertsEnabled: Boolean = true,
    val budgetWarningsEnabled: Boolean = true,
    val subscription: SubscriptionUiState = SubscriptionUiState(),
    val cloudSyncState: CloudSyncState = CloudSyncState.IDLE,
    val statusMessage: String? = null
)

