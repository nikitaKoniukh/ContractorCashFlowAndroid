package com.yetzira.ContractorCashFlowAndroid.ui.settings

import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption

enum class CloudSyncState {
    IDLE,
    SYNCING,
    DONE,
    FAILED
}

data class SubscriptionUiState(
    val isPro: Boolean = false,
    val planName: String = "",
    val renewalDate: Long? = null
)

data class SettingsUiState(
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val selectedLanguage: AppLanguageOption = AppLanguageOption.HEBREW,
    val selectedThemeMode: ThemeModeOption = ThemeModeOption.SYSTEM,
    val selectedCurrency: CurrencyOption = CurrencyOption.ILS,
    val invoiceRemindersEnabled: Boolean = true,
    val overdueAlertsEnabled: Boolean = true,
    val budgetWarningsEnabled: Boolean = true,
    val subscription: SubscriptionUiState = SubscriptionUiState(),
    val cloudSyncState: CloudSyncState = CloudSyncState.IDLE,
    val statusMessage: String? = null
)

