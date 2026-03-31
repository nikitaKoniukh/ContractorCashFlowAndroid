package com.yetzira.ContractorCashFlowAndroid.ui.settings

import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.SettingsPreferencesRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import com.yetzira.ContractorCashFlowAndroid.export.DataExportServiceContract
import com.yetzira.ContractorCashFlowAndroid.network.NetworkConnectivityCheckerContract
import com.yetzira.ContractorCashFlowAndroid.notification.NotificationSettingsCoordinatorContract
import com.yetzira.ContractorCashFlowAndroid.sync.CloudSyncServiceContract
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `runCloudSync requires authenticated user`() = runTest {
        val auth = FakeAuthGateway(current = null)
        val vm = createViewModel(authGateway = auth)
        val collectJob = launch { vm.uiState.collect { } }

        vm.runCloudSync()
        advanceUntilIdle()

        assertEquals(CloudSyncState.FAILED, vm.uiState.value.cloudSyncState)
        assertEquals("SIGN_IN_REQUIRED", vm.uiState.value.statusMessage)

        collectJob.cancel()
    }

    @Test
    fun `runCloudSync fails when offline`() = runTest {
        val auth = FakeAuthGateway(current = AuthUser("uid-1", "a@mail.com"))
        val vm = createViewModel(
            authGateway = auth,
            networkChecker = FakeNetworkChecker(false)
        )
        val collectJob = launch { vm.uiState.collect { } }

        vm.runCloudSync()
        advanceUntilIdle()

        assertEquals(CloudSyncState.FAILED, vm.uiState.value.cloudSyncState)
        assertEquals("NO_INTERNET", vm.uiState.value.statusMessage)

        collectJob.cancel()
    }

    @Test
    fun `runCloudSync success updates sync state and status`() = runTest {
        val vm = createViewModel(
            authGateway = FakeAuthGateway(current = AuthUser("uid-1", "a@mail.com")),
            syncService = FakeCloudSyncService(fullSyncResult = Result.success(Unit))
        )
        val collectJob = launch { vm.uiState.collect { } }

        vm.runCloudSync()
        advanceUntilIdle()

        assertEquals(CloudSyncState.DONE, vm.uiState.value.cloudSyncState)
        assertEquals("SYNC_DONE", vm.uiState.value.statusMessage)

        collectJob.cancel()
    }

    @Test
    fun `notification toggles reschedule and persist settings`() = runTest {
        val prefs = FakeSettingsPreferencesRepository()
        val coordinator = FakeNotificationSettingsCoordinator()
        val vm = createViewModel(preferences = prefs, coordinator = coordinator)

        val collectJob = launch { vm.uiState.collect { } }

        vm.setInvoiceRemindersEnabled(false)
        advanceUntilIdle()

        assertFalse(prefs.invoiceRemindersEnabledFlow.value)
        assertTrue(coordinator.calls.isNotEmpty())
        assertEquals("NOTIFICATIONS_UPDATED", vm.uiState.value.statusMessage)

        collectJob.cancel()
    }

    @Test
    fun `suggestedExportFileName delegates to exporter`() = runTest {
        val vm = createViewModel(exporter = FakeExporter(Result.success(Unit)))
        assertEquals("KablanPro_Export_2026-03-21.json", vm.suggestedExportFileName())
    }

    private fun createViewModel(
        preferences: FakeSettingsPreferencesRepository = FakeSettingsPreferencesRepository(),
        coordinator: FakeNotificationSettingsCoordinator = FakeNotificationSettingsCoordinator(),
        syncService: FakeCloudSyncService = FakeCloudSyncService(),
        exporter: FakeExporter = FakeExporter(Result.success(Unit)),
        authGateway: FakeAuthGateway = FakeAuthGateway(current = null),
        networkChecker: FakeNetworkChecker = FakeNetworkChecker(true)
    ): SettingsViewModel {
        return SettingsViewModel(
            preferencesRepository = preferences,
            notificationSettingsCoordinator = coordinator,
            firestoreSyncService = syncService,
            exporter = exporter,
            authGateway = authGateway,
            networkConnectivityChecker = networkChecker,
            stringResolver = FakeStringResolver()
        )
    }

    private class FakeSettingsPreferencesRepository : SettingsPreferencesRepositoryContract {
        val appLanguageFlow = MutableStateFlow(AppLanguageOption.HEBREW)
        val themeModeFlow = MutableStateFlow(ThemeModeOption.SYSTEM)
        val selectedCurrencyFlow = MutableStateFlow(CurrencyOption.ILS)
        val invoiceRemindersEnabledFlow = MutableStateFlow(false)
        val overdueAlertsEnabledFlow = MutableStateFlow(false)
        val budgetWarningsEnabledFlow = MutableStateFlow(false)
        val subscriptionIsProFlow = MutableStateFlow(false)
        val subscriptionPlanNameFlow = MutableStateFlow<String?>(null)
        val subscriptionRenewalDateFlow = MutableStateFlow<Long?>(null)

        override val appLanguage: Flow<AppLanguageOption> = appLanguageFlow
        override val themeMode: Flow<ThemeModeOption> = themeModeFlow
        override val selectedCurrencyCode: Flow<CurrencyOption> = selectedCurrencyFlow
        override val invoiceRemindersEnabled: Flow<Boolean> = invoiceRemindersEnabledFlow
        override val overdueAlertsEnabled: Flow<Boolean> = overdueAlertsEnabledFlow
        override val budgetWarningsEnabled: Flow<Boolean> = budgetWarningsEnabledFlow
        override val subscriptionIsPro: Flow<Boolean> = subscriptionIsProFlow
        override val subscriptionPlanName: Flow<String?> = subscriptionPlanNameFlow
        override val subscriptionRenewalDate: Flow<Long?> = subscriptionRenewalDateFlow

        override suspend fun setAppLanguage(language: AppLanguageOption) {
            appLanguageFlow.value = language
        }

        override suspend fun setThemeMode(themeMode: ThemeModeOption) {
            themeModeFlow.value = themeMode
        }

        override suspend fun setSelectedCurrency(currency: CurrencyOption) {
            selectedCurrencyFlow.value = currency
        }

        override suspend fun setInvoiceRemindersEnabled(enabled: Boolean) {
            invoiceRemindersEnabledFlow.value = enabled
        }

        override suspend fun setOverdueAlertsEnabled(enabled: Boolean) {
            overdueAlertsEnabledFlow.value = enabled
        }

        override suspend fun setBudgetWarningsEnabled(enabled: Boolean) {
            budgetWarningsEnabledFlow.value = enabled
        }
    }

    private class FakeNotificationSettingsCoordinator : NotificationSettingsCoordinatorContract {
        val calls = mutableListOf<Triple<Boolean, Boolean, Boolean>>()

        override suspend fun rescheduleAll(
            invoiceRemindersEnabled: Boolean,
            overdueAlertsEnabled: Boolean,
            budgetWarningsEnabled: Boolean
        ) {
            calls += Triple(invoiceRemindersEnabled, overdueAlertsEnabled, budgetWarningsEnabled)
        }
    }


    private class FakeCloudSyncService(
        private val fullSyncResult: Result<Unit> = Result.success(Unit),
        private val pushResult: Result<Unit> = Result.success(Unit)
    ) : CloudSyncServiceContract {
        override suspend fun pushAllData(): Result<Unit> = pushResult
        override suspend fun fullSync(): Result<Unit> = fullSyncResult
    }

    private class FakeExporter(
        private val exportResult: Result<Unit>
    ) : DataExportServiceContract {
        override suspend fun exportToUri(uri: android.net.Uri): Result<Unit> = exportResult

        override fun suggestedFileName(now: Long): String = "KablanPro_Export_2026-03-21.json"
    }

    private class FakeAuthGateway(current: AuthUser?) : AuthGateway {
        private var user: AuthUser? = current
        private val listeners = mutableListOf<(AuthUser?) -> Unit>()

        override val currentUser: AuthUser?
            get() = user

        override fun addAuthStateListener(listener: (AuthUser?) -> Unit) {
            listeners += listener
        }

        override fun removeAuthStateListener(listener: (AuthUser?) -> Unit) {
            listeners.remove(listener)
        }

        override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser?> {
            user = AuthUser(uid = "uid", email = "user@mail.com")
            listeners.forEach { it(user) }
            return Result.success(user)
        }

        override fun signOut() {
            user = null
            listeners.forEach { it(null) }
        }
    }

    private class FakeNetworkChecker(
        private val online: Boolean
    ) : NetworkConnectivityCheckerContract {
        override fun canAttemptNetworkCall(): Boolean = online
    }

    private class FakeStringResolver : SettingsStringResolver {
        override fun getString(resId: Int, vararg formatArgs: Any): String {
            return when (resId) {
                R.string.settings_status_sign_in_required_for_sync -> "SIGN_IN_REQUIRED"
                R.string.settings_status_no_internet -> "NO_INTERNET"
                R.string.settings_sync_done -> "SYNC_DONE"
                R.string.settings_sync_failed -> "SYNC_FAILED"
                R.string.settings_status_export_success -> "EXPORT_SUCCESS"
                R.string.settings_status_export_failed -> "EXPORT_FAILED"
                R.string.settings_status_notifications_updated -> "NOTIFICATIONS_UPDATED"
                R.string.settings_subscription_plan_pro_default -> "KablanPro Pro"
                R.string.settings_subscription_free -> "Free Plan"
                else -> "RES_$resId"
            }
        }
    }
}




