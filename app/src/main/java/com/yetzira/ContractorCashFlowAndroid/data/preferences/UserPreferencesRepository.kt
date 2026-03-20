package com.yetzira.ContractorCashFlowAndroid.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "kablan_pro_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    // Preference Keys
    private companion object {
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val SELECTED_CURRENCY_CODE = stringPreferencesKey("selected_currency_code")
        val INVOICE_REMINDERS_ENABLED = booleanPreferencesKey("invoice_reminders_enabled")
        val OVERDUE_ALERTS_ENABLED = booleanPreferencesKey("overdue_alerts_enabled")
        val BUDGET_WARNINGS_ENABLED = booleanPreferencesKey("budget_warnings_enabled")
    }

    // Flow getters
    val appLanguage: Flow<AppLanguageOption> = dataStore.data.map { preferences ->
        val code = preferences[APP_LANGUAGE] ?: "he"
        AppLanguageOption.fromCode(code)
    }

    val selectedCurrencyCode: Flow<CurrencyOption> = dataStore.data.map { preferences ->
        val code = preferences[SELECTED_CURRENCY_CODE] ?: "ILS"
        CurrencyOption.fromCode(code)
    }

    val invoiceRemindersEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[INVOICE_REMINDERS_ENABLED] ?: true
    }

    val overdueAlertsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[OVERDUE_ALERTS_ENABLED] ?: true
    }

    val budgetWarningsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BUDGET_WARNINGS_ENABLED] ?: true
    }

    // Suspend setters
    suspend fun setAppLanguage(language: AppLanguageOption) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language.code
        }
    }

    suspend fun setSelectedCurrency(currency: CurrencyOption) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY_CODE] = currency.code
        }
    }

    suspend fun setInvoiceRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[INVOICE_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setOverdueAlertsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OVERDUE_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun setBudgetWarningsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BUDGET_WARNINGS_ENABLED] = enabled
        }
    }
}

