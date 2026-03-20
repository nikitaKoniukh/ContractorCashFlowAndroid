package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.export.AppDataSnapshotExporter
import com.yetzira.ContractorCashFlowAndroid.notification.NotificationSettingsCoordinator
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationSettingsCoordinator: NotificationSettingsCoordinator,
    private val firestoreSyncService: FirestoreSyncService,
    private val exporter: AppDataSnapshotExporter
) : ViewModel() {

    private val syncState = MutableStateFlow(CloudSyncState.IDLE)
    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.appLanguage,
        preferencesRepository.selectedCurrencyCode,
        preferencesRepository.invoiceRemindersEnabled,
        preferencesRepository.overdueAlertsEnabled,
        preferencesRepository.budgetWarningsEnabled,
        preferencesRepository.subscriptionIsPro,
        preferencesRepository.subscriptionPlanName,
        preferencesRepository.subscriptionRenewalDate,
        syncState,
        statusMessage
    ) { values ->
        val language = values[0] as AppLanguageOption
        val currency = values[1] as CurrencyOption
        val invoiceReminders = values[2] as Boolean
        val overdueAlerts = values[3] as Boolean
        val budgetWarnings = values[4] as Boolean
        val isPro = values[5] as Boolean
        val planName = values[6] as String?
        val renewalDate = values[7] as Long?
        val cloudSyncState = values[8] as CloudSyncState
        val message = values[9] as String?

        SettingsUiState(
            selectedLanguage = language,
            selectedCurrency = currency,
            invoiceRemindersEnabled = invoiceReminders,
            overdueAlertsEnabled = overdueAlerts,
            budgetWarningsEnabled = budgetWarnings,
            subscription = SubscriptionUiState(
                isPro = isPro,
                planName = if (isPro) (planName ?: "KablanPro Pro") else "Free Plan",
                renewalDate = renewalDate
            ),
            cloudSyncState = cloudSyncState,
            statusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setLanguage(language: AppLanguageOption) {
        viewModelScope.launch {
            preferencesRepository.setAppLanguage(language)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
            statusMessage.value = "Language updated"
        }
    }

    fun setCurrency(currency: CurrencyOption) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCurrency(currency)
            statusMessage.value = "Currency updated"
        }
    }

    fun setInvoiceRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setInvoiceRemindersEnabled(enabled)
            rescheduleNotifications()
        }
    }

    fun setOverdueAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOverdueAlertsEnabled(enabled)
            rescheduleNotifications()
        }
    }

    fun setBudgetWarningsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBudgetWarningsEnabled(enabled)
            rescheduleNotifications()
        }
    }

    fun runCloudSync() {
        viewModelScope.launch {
            syncState.value = CloudSyncState.SYNCING
            val result = firestoreSyncService.pullAllData()
            syncState.value = if (result.isSuccess) CloudSyncState.DONE else CloudSyncState.FAILED
            statusMessage.value = result.exceptionOrNull()?.message ?: "Cloud sync completed"
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val result = exporter.exportToUri(uri)
            statusMessage.value = if (result.isSuccess) {
                "Data exported successfully"
            } else {
                result.exceptionOrNull()?.message ?: "Export failed"
            }
        }
    }

    fun suggestedExportFileName(): String = exporter.suggestedFileName()

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private suspend fun rescheduleNotifications() {
        notificationSettingsCoordinator.rescheduleAll(
            invoiceRemindersEnabled = preferencesRepository.invoiceRemindersEnabled.first(),
            overdueAlertsEnabled = preferencesRepository.overdueAlertsEnabled.first(),
            budgetWarningsEnabled = preferencesRepository.budgetWarningsEnabled.first()
        )
        statusMessage.value = "Notification settings updated"
    }
}

