package com.yetzira.ContractorCashFlowAndroid.ui.settings

import com.yetzira.ContractorCashFlowAndroid.data.preferences.AppLanguageOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.ThemeModeOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUiModelsTest {

    @Test
    fun `settings ui state defaults match kablanpro expectations`() {
        val state = SettingsUiState()

        assertFalse(state.isAuthenticated)
        assertNull(state.userEmail)
        assertEquals(AppLanguageOption.HEBREW, state.selectedLanguage)
        assertEquals(CurrencyOption.ILS, state.selectedCurrency)
        assertEquals(ThemeModeOption.SYSTEM, state.selectedThemeMode)
        assertTrue(state.invoiceRemindersEnabled)
        assertTrue(state.overdueAlertsEnabled)
        assertTrue(state.budgetWarningsEnabled)
        assertEquals(CloudSyncState.IDLE, state.cloudSyncState)
    }

    @Test
    fun `subscription ui state defaults to free plan`() {
        val subscription = SubscriptionUiState()
        assertFalse(subscription.isPro)
        assertEquals("", subscription.planName)
        assertNull(subscription.renewalDate)
    }
}

