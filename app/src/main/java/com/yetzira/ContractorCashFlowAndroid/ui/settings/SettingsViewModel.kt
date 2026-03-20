package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.export.AppDataSnapshotExporter
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
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationSettingsCoordinator: NotificationSettingsCoordinator,
    private val firestoreSyncService: FirestoreSyncService,
    private val exporter: AppDataSnapshotExporter,
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
            Log.d(SETTINGS_AUTH_LOG_TAG, "Applying app locale code=${language.code}")
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
            statusMessage.value = "Language updated"
        }
    }

    fun setThemeMode(themeMode: ThemeModeOption) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
            AppCompatDelegate.setDefaultNightMode(themeMode.nightModeValue)
            statusMessage.value = "Theme updated"
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
                    statusMessage.value = "Signed in, but cloud sync is unavailable. Check internet connection and DNS settings, then try Sync again."
                    return@onSuccess
                }
                // Automatically push all existing local data to Firestore on first sign-in
                statusMessage.value = "Signed in — uploading local data…"
                syncState.value = CloudSyncState.SYNCING
                val pushResult = runCatching {
                    withTimeout(CLOUD_SYNC_TIMEOUT_MS) {
                        firestoreSyncService.pushAllData().getOrThrow()
                    }
                }
                if (pushResult.isSuccess) {
                    Log.d(SETTINGS_AUTH_LOG_TAG, "Auto push-on-sign-in succeeded")
                    syncState.value = CloudSyncState.DONE
                    statusMessage.value = "Signed in and local data synced to cloud"
                } else {
                    val err = pushResult.exceptionOrNull()?.message ?: "Upload failed"
                    Log.e(SETTINGS_AUTH_LOG_TAG, "Auto push-on-sign-in failed: $err")
                    syncState.value = CloudSyncState.FAILED
                    statusMessage.value = "Signed in, but sync failed: $err"
                }
            }.onFailure { throwable ->
                Log.e(SETTINGS_AUTH_LOG_TAG, "Firebase sign-in failed", throwable)
                statusMessage.value = throwable.message ?: "Google sign-in failed"
            }
        }
    }

    fun onGoogleSignInFailed(message: String) {
        Log.w(SETTINGS_AUTH_LOG_TAG, "Google sign-in failure surfaced to UI: $message")
        statusMessage.value = message
    }

    fun signOut() {
        firebaseAuth.signOut()
        statusMessage.value = "Signed out"
    }

    fun runCloudSync() {
        viewModelScope.launch {
            if (firebaseAuth.currentUser == null) {
                syncState.value = CloudSyncState.FAILED
                statusMessage.value = "Sign in with Google to sync cloud data"
                return@launch
            }
            if (!networkConnectivityChecker.canAttemptNetworkCall()) {
                syncState.value = CloudSyncState.FAILED
                statusMessage.value = "No internet connection. Check Wi-Fi/mobile data or Private DNS settings, then try again."
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
                "Cloud sync completed"
            } else {
                result.exceptionOrNull()?.message ?: "Sync failed"
            }
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

private const val SETTINGS_AUTH_LOG_TAG = "KablanProAuth"
private const val CLOUD_SYNC_TIMEOUT_MS = 30_000L

