package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.export.DataExportService
import com.yetzira.ContractorCashFlowAndroid.network.NetworkConnectivityChecker
import com.yetzira.ContractorCashFlowAndroid.notification.NotificationSettingsCoordinator
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(
    private val appContext: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationSettingsCoordinator: NotificationSettingsCoordinator,
    private val firestoreSyncService: FirestoreSyncService,
    private val exporter: DataExportService,
    private val firebaseAuth: FirebaseAuth,
    private val networkConnectivityChecker: NetworkConnectivityChecker
) : ViewModel() {

    private val syncState = MutableStateFlow(CloudSyncState.IDLE)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val authUser = MutableStateFlow(firebaseAuth.currentUser)

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        authUser.value = auth.currentUser
        Log.d(
            SETTINGS_AUTH_LOG_TAG,
            "Auth state changed signedIn=${auth.currentUser != null} uid=${auth.currentUser?.uid.orEmpty()} email=${auth.currentUser?.email.orEmpty()}"
        )
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        authUser,
        preferencesRepository.appLanguage,
        preferencesRepository.themeMode,
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
        val user = values[0] as com.google.firebase.auth.FirebaseUser?
        val language = values[1] as AppLanguageOption
        val themeMode = values[2] as ThemeModeOption
        val currency = values[3] as CurrencyOption
        val invoiceReminders = values[4] as Boolean
        val overdueAlerts = values[5] as Boolean
        val budgetWarnings = values[6] as Boolean
        val isPro = values[7] as Boolean
        val planName = values[8] as String?
        val renewalDate = values[9] as Long?
        val cloudSyncState = values[10] as CloudSyncState
        val message = values[11] as String?

        SettingsUiState(
            isAuthenticated = user != null,
            userEmail = user?.email,
            selectedLanguage = language,
            selectedThemeMode = themeMode,
            selectedCurrency = currency,
            invoiceRemindersEnabled = invoiceReminders,
            overdueAlertsEnabled = overdueAlerts,
            budgetWarningsEnabled = budgetWarnings,
            subscription = SubscriptionUiState(
                isPro = isPro,
                planName = if (isPro) {
                    planName ?: appContext.getString(R.string.settings_subscription_plan_pro_default)
                } else {
                    appContext.getString(R.string.settings_subscription_free)
                },
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
            Log.d(SETTINGS_AUTH_LOG_TAG, "Applying app locale code=${language.code}")
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
            showStatus(R.string.settings_status_language_updated)
        }
    }

    fun setThemeMode(themeMode: ThemeModeOption) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
            AppCompatDelegate.setDefaultNightMode(themeMode.nightModeValue)
            showStatus(R.string.settings_status_theme_updated)
        }
    }

    fun setCurrency(currency: CurrencyOption) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCurrency(currency)
            showStatus(R.string.settings_status_currency_updated)
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

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            Log.d(SETTINGS_AUTH_LOG_TAG, "Firebase credential sign-in started idTokenLength=${idToken.length}")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = runCatching { firebaseAuth.signInWithCredential(credential).await() }
            result.onSuccess { authResult ->
                Log.d(
                    SETTINGS_AUTH_LOG_TAG,
                    "Firebase sign-in success uid=${authResult.user?.uid.orEmpty()} email=${authResult.user?.email.orEmpty()}"
                )
                if (!networkConnectivityChecker.canAttemptNetworkCall()) {
                    syncState.value = CloudSyncState.FAILED
                    showStatus(R.string.settings_status_signed_in_sync_unavailable)
                    return@onSuccess
                }
                // Automatically push all existing local data to Firestore on first sign-in
                showStatus(R.string.settings_status_signed_in_uploading)
                syncState.value = CloudSyncState.SYNCING
                val pushResult = runCatching {
                    withTimeout(CLOUD_SYNC_TIMEOUT_MS) {
                        firestoreSyncService.pushAllData().getOrThrow()
                    }
                }
                if (pushResult.isSuccess) {
                    Log.d(SETTINGS_AUTH_LOG_TAG, "Auto push-on-sign-in succeeded")
                    syncState.value = CloudSyncState.DONE
                    showStatus(R.string.settings_status_signed_in_synced)
                } else {
                    val err = pushResult.exceptionOrNull()?.message
                        ?: appContext.getString(R.string.settings_status_upload_failed)
                    Log.e(SETTINGS_AUTH_LOG_TAG, "Auto push-on-sign-in failed: $err")
                    syncState.value = CloudSyncState.FAILED
                    showStatus(R.string.settings_status_signed_in_sync_failed, err)
                }
            }.onFailure { throwable ->
                Log.e(SETTINGS_AUTH_LOG_TAG, "Firebase sign-in failed", throwable)
                statusMessage.value = throwable.message
                    ?: appContext.getString(R.string.settings_google_sign_in_failed)
            }
        }
    }

    fun onGoogleSignInFailed(message: String) {
        Log.w(SETTINGS_AUTH_LOG_TAG, "Google sign-in failure surfaced to UI: $message")
        statusMessage.value = message
    }

    fun signOut() {
        firebaseAuth.signOut()
        showStatus(R.string.settings_status_signed_out)
    }

    fun runCloudSync() {
        viewModelScope.launch {
            if (firebaseAuth.currentUser == null) {
                syncState.value = CloudSyncState.FAILED
                showStatus(R.string.settings_status_sign_in_required_for_sync)
                return@launch
            }
            if (!networkConnectivityChecker.canAttemptNetworkCall()) {
                syncState.value = CloudSyncState.FAILED
                showStatus(R.string.settings_status_no_internet)
                return@launch
            }
            syncState.value = CloudSyncState.SYNCING
            val result = runCatching {
                withTimeout(CLOUD_SYNC_TIMEOUT_MS) {
                    firestoreSyncService.fullSync().getOrThrow()
                }
            }
            syncState.value = if (result.isSuccess) CloudSyncState.DONE else CloudSyncState.FAILED
            statusMessage.value = if (result.isSuccess) {
                appContext.getString(R.string.settings_sync_done)
            } else {
                result.exceptionOrNull()?.message
                    ?: appContext.getString(R.string.settings_sync_failed)
            }
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            val result = exporter.exportToUri(uri)
            statusMessage.value = if (result.isSuccess) {
                appContext.getString(R.string.settings_status_export_success)
            } else {
                result.exceptionOrNull()?.message
                    ?: appContext.getString(R.string.settings_status_export_failed)
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
        showStatus(R.string.settings_status_notifications_updated)
    }

    private fun showStatus(@StringRes resId: Int, vararg formatArgs: Any) {
        statusMessage.value = appContext.getString(resId, *formatArgs)
    }
}

private const val SETTINGS_AUTH_LOG_TAG = "KablanProAuth"
private const val CLOUD_SYNC_TIMEOUT_MS = 30_000L

